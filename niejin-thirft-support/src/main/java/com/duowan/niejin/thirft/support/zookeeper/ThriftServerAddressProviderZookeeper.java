package com.duowan.niejin.thirft.support.zookeeper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.duowan.niejin.thirft.support.balance.ServiceLoadBalanceStrategy;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月7日
 *
**/
public class ThriftServerAddressProviderZookeeper implements ThriftServerAddressProvider,InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(ThriftServerAddressProviderZookeeper.class);

	//注册服务
	private String service;
	//服务版本
	private String version;
	
	//zk 客户端
	private CuratorFramework zkClient ;
	//zookeeper Factory
	private ZookeeperFactory zookeeperFactory;

	private ServiceLoadBalanceStrategy serviceBalance;
	
	private static CountDownLatch countDownLatch = new CountDownLatch(1);//避免 zk 还没有连接上 就去调用服务了
	
	private final List<InetSocketAddress> addressContainer = new ArrayList<InetSocketAddress>();
	//用来保存当前provider所接触的地址记录，当zookeeper集群故障时，可以使用localTraceContainer中地址作为备份
	private	Set<String> localTraceContainer = new HashSet<String>();
	
	private Object lock = new Object();
	
	private PathChildrenCache zkCachedPath;
	
	private boolean isInited = false;
	
	public ThriftServerAddressProviderZookeeper(){}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(!isInited){
			init();	
		}
	}
	
	//@PostConstruct
	public void init() throws Exception{
		System.out.println("ThriftServerAddressProviderZookeeper init ...");
		
		if(zkClient == null){
			zkClient = zookeeperFactory.getObject();
		}
		
		if(zkClient.getState() == CuratorFrameworkState.LATENT){
			zkClient.start();
		}

		this.buildZookeeperClientConnectionStateListen(zkClient);
		
		this.buildPatchChildrenCache(zkClient, this.getServicePath(), true);
		
		zkCachedPath.start(StartMode.POST_INITIALIZED_EVENT);
		//同步 等待
		synchronized(this){
			countDownLatch.await();
		}
	}

	/**
	 * UNDO
	 * @param client
	 */
	private void buildZookeeperClientConnectionStateListen(final CuratorFramework client){
		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				System.out.println("client state:" + newState.name());
				switch (newState) {
				case CONNECTED:	break;
				case READ_ONLY:	break;
				case LOST : /* 可是 断线重连 (注册)*/ break;
				case RECONNECTED :break;
				case SUSPENDED: break;
				default: break;
				}
			}
		});
	}
	
	private void buildPatchChildrenCache(final CuratorFramework client,String path,Boolean cacheData){
		
		zkCachedPath = new PathChildrenCache(client,path,cacheData);
		
		zkCachedPath.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework zk, PathChildrenCacheEvent event) throws Exception {
				PathChildrenCacheEvent.Type eventType = event.getType();
				switch (eventType) {
				case CONNECTION_RECONNECTED:
					logger.info("zk connection is reconnected");
					break;
				case CONNECTION_SUSPENDED:
					logger.info("zk connection is suspended");
					break;
				case CONNECTION_LOST:
					logger.info("zk connection is lost");
					break;
				case INITIALIZED:
					logger.info("zk connection initialized");
				default:
					break;// TODO
				}
				//任何节点的数据变法，都rebuild //simple
				zkCachedPath.rebuild();
				
				this.rebuild();
				//countDown sign
				countDownLatch.countDown();
			}
			
			protected void rebuild() throws Exception {
				List<ChildData> children = zkCachedPath.getCurrentData();
				if (children == null || children.isEmpty()) {
					// 有可能所有的thrift server都与zookeeper断开了链接
					// 但是,有可能,thrift client与thrift server之间的网络是良好的
					// 因此此处是否需要清空addressContainer,是需要多方面考虑的.
					addressContainer.clear();
					logger.error("thrift rpc-server error ...");
					return;
				}

				List<InetSocketAddress> current = new ArrayList<InetSocketAddress>();
				for(ChildData child : children){
					String path = child.getPath();
					String data = new String(child.getData(),"utf-8");
					logger.debug("zookeeper node path:{},data:{}",path,data);
					try {
						InetSocketAddress address = transfer(data);
						if(address != null){
							localTraceContainer.add(data);
							current.add(address);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}

				synchronized (lock) {
                    addressContainer.clear();
                    addressContainer.addAll(current);
                }
				  
			}
		});
	}
	
	private String getServicePath(){
		return "/" + this.getService() + "/" + this.getVesion();
	}
	
	public String getVesion(){
		return this.version;
	}
	
	@Override
	public String getService() {
		return this.service;
	}

	@Override
	public List<InetSocketAddress> findServerAddressList() {
		return (List<InetSocketAddress>) Collections.unmodifiableCollection(addressContainer);
	}

	@Override
	public InetSocketAddress selector() {
		InetSocketAddress address = null;
		synchronized (lock) {
			if(this.addressContainer.isEmpty()){
				if(!localTraceContainer.isEmpty()){
					for(String hostAddress : localTraceContainer){
						addressContainer.add(transfer(hostAddress));
					}
				}
			}
			if(!this.addressContainer.isEmpty()){
				address = serviceBalance.select(addressContainer);
			}
		}
		return address;
	}
	
	private InetSocketAddress transfer(String hostAddress){
		if(StringUtils.isBlank(hostAddress))
			return null;
		try {
			String[] hostname = hostAddress.split(":");
			return new InetSocketAddress(hostname[0], Integer.parseInt(hostname[1]));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	@Override
	public void close() {
		try {
			zkCachedPath.close();
			zkClient.close();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public CuratorFramework getZkClient() {
		return zkClient;
	}

	public void setZkClient(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}

	public void setService(String service) {
		this.service = service;
	}
	
	public void setZookeeperFactory(ZookeeperFactory zookeeperFactory){
		this.zookeeperFactory = zookeeperFactory;
	}
	
	public ServiceLoadBalanceStrategy getServiceBalance() {
		return serviceBalance;
	}

	public void setServiceBalance(ServiceLoadBalanceStrategy serviceBalance) {
		this.serviceBalance = serviceBalance;
	}

	public static void main(String[] args) throws Exception {
		CuratorFramework zk = CuratorFrameworkFactory.builder().connectString("172.17.5.181:2181")
				.sessionTimeoutMs(30000).connectionTimeoutMs(30000).canBeReadOnly(false)
				.retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE)).namespace("rpc" + "/" + "thrift.rpc")
				.defaultData(null).build();
		
		ThriftServerAddressProviderZookeeper provider = new ThriftServerAddressProviderZookeeper();
		provider.setZkClient(zk);
		provider.setService("com.duowan.niejin.thrift.UserService");
		provider.setVersion("1.0.0");
		provider.init();
		System.out.println("#############################" + provider.selector());
	}

}
