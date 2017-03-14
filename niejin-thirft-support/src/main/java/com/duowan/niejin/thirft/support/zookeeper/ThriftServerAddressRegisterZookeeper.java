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
	public void register(String service, String version, String address) throws ThriftException {
		if (zkClient.getState() == CuratorFrameworkState.LATENT) {
			zkClient.start();
		}

		if (StringUtils.isBlank(version)) {
			version = "1.0.0";
		}

		// 创建临时的节点
		try {
			Stat stat = zkClient.checkExists().forPath("/" + service + "/" + version);
			if (stat == null) {
				//Service 根目录创建为PERSISTENT[持久化]
				zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
						.forPath("/" + service + "/" + version);
			}
			
			zkClient.create().creatingParentsIfNeeded()
							.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
							.forPath("/" + service + "/" + version + "/" + "node",address.getBytes());
			
			zkClient.getCuratorListenable().addListener(new CuratorListener() {
				@Override
				public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
					System.out.println("zookeeper register event :" + event.getName());
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
