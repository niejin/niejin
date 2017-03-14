package com.duowan.niejin.thirft.support.zookeeper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duowan.niejin.thirft.support.ThriftException;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月7日 注册服务列表到Zookeeper
 **/
public class ThriftServerAddressRegisterZookeeper implements ThriftServerAddressRegister {

	private Logger logger = LoggerFactory.getLogger(ThriftServerAddressRegisterZookeeper.class);

	private CuratorFramework zkClient;

	public ThriftServerAddressRegisterZookeeper() {}

	public ThriftServerAddressRegisterZookeeper(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}

	public CuratorFramework getZkClient() {
		return zkClient;
	}

	public void setZkClient(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}

	@Override
	public void register(String service,String version, final String address) throws ThriftException {
		if (zkClient.getState() == CuratorFrameworkState.LATENT) {
			zkClient.start();
		}

		if (StringUtils.isBlank(version)) {
			version = "1.0.0";
		}

		final String path = "/" + service + "/" + version;
		// 创建临时的节点
		try {
			Stat stat = zkClient.checkExists().forPath(path);
			if (stat == null) {
				//Service 根目录创建为PERSISTENT[持久化]
				zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
						.forPath(path);
			}
			
			zkClient.create().creatingParentsIfNeeded()
							.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
							.forPath(path + "/" + "node",address.getBytes());
			
			zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
				@Override
				public void stateChanged(CuratorFramework client, ConnectionState newState) {
					System.out.println("Zookeeper register connection status : " + newState.name());
					//session 过期 导致LOST 重连.
					if(newState == ConnectionState.LOST){
						try {
							client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
									.forPath(path + "/" + "node", address.getBytes());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ThriftException("register thrift service to zookeeper exception {}", e);
		}
	}

	public void close() {
		zkClient.close();
	}
}
