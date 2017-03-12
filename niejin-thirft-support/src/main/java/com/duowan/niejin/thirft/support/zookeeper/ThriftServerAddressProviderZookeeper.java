package com.duowan.niejin.thirft.support.zookeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	private static CountDownLatch countDownLatch = new CountDownLatch(1);//避免 zk 还没有连接上 就去调用服务了
	
	//用来保存当前provider所接触的地址记录，当zookeeper集群故障时，可以使用trace中地址作为备份
	private Set<String> trace = new HashSet<String>();
	
	private final List<InetSocketAddress> addressContainer = new ArrayList<InetSocketAddress>();
	
	private Queue<InetSocketAddress> inner = new LinkedList<InetSocketAddress>();
	
	private Object lock = new Object();
	
	private static final Integer DEFAULT_WEIGHT = 1;
	
	private PathChildrenCache zkCachedPath;
	
	private static boolean isInited = false;
	
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
		if(zkClient.getState() == CuratorFrameworkState.LATENT){
			zkClient.start();
		}
		
		this.buildPatchChildrenCache(zkClient, this.getServicePath(), true);
		
		zkCachedPath.start(StartMode.POST_INITIALIZED_EVENT);
		//同步 等待
		synchronized(ThriftServerAddressProviderZookeeper.class){
			System.out.println("########### CountDownLatch await");
			countDownLatch.await();
		}
	}

	private void buildPatchChildrenCache(final CuratorFramework client,String path,Boolean cacheData){
		System.out.println("######## buildPatchChildrenCache");
		zkCachedPath = new PathChildrenCache(client,path,cacheData);
		zkCachedPath.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework zk, PathChildrenCacheEvent event) throws Exception {
				PathChildrenCacheEvent.Type eventType = event.getType();
				System.out.println("######## PathChildrenCacheEvent:" + eventType);
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
					// TODO
					break;
				}
				
				//任何节点的数据变法，都rebuild //simple
				zkCachedPath.rebuild();
				//
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
				String path = null;
				for(ChildData data : children){
					path = data.getPath();
					logger.debug("zk get child path : {}",path);
					path = path.substring(getServicePath().length() + 1);
					logger.debug("zk get child path serviceAddress : {}",path);
					String address = new String(path.getBytes(),"utf-8");
					
					current.addAll(transfer(address));
					trace.add(address);
				}

				Collections.shuffle(current);
				synchronized (lock) {
                    addressContainer.clear();
                    addressContainer.addAll(current);
                    inner.clear();
                    inner.addAll(current);
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
		//WHY TODO
		return (List<InetSocketAddress>) Collections.unmodifiableCollection(addressContainer);
	}

	@Override
	public InetSocketAddress selector() {
		if(inner.isEmpty()){
			synchronized(lock){
				if(!addressContainer.isEmpty()){
					inner.addAll(addressContainer);
				}else if(!trace.isEmpty()){
					for (String hostAddress : trace) {
						addressContainer.addAll(transfer(hostAddress));
                    }
					//随机排列 addressContainer
                    Collections.shuffle(addressContainer);
                    inner.addAll(addressContainer);
				}
			}
		}
		return inner.poll();
	}
	
	private List<InetSocketAddress> transfer(String hostAddress) {
		if(StringUtils.isBlank(hostAddress))
			return null;
		
		String[] hostname = hostAddress.split(":");
		Integer weight = DEFAULT_WEIGHT;
		if(hostname.length == 3){
			weight = Integer.valueOf(hostname[2]);
		}
		List<InetSocketAddress> items = new ArrayList<InetSocketAddress>();
		// 根据优先级，将ip：port添加多次到地址集中，然后随机取地址实现负载
		for(int i = 0 ; i < weight ;i++){
			items.add(new InetSocketAddress(hostname[0], Integer.parseInt(hostname[1])));
		}
		return items;
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
