package com.duowan.niejin.thrift.client.conf;

import java.util.Random;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duowan.niejin.thirft.support.ThriftServiceClientProxyFactory;
import com.duowan.niejin.thirft.support.balance.RandomStrategy;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressProvider;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressProviderZookeeper;
import com.duowan.niejin.thirft.support.zookeeper.ZookeeperFactory;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月12日
 *
**/
@Configuration
public class ClientConfiguration {
	private String zkHost = "172.17.5.181";
	private Integer zkPort = 2181;
	private String namespace = "thrift.rpc";
	private Integer sessionTimeout = 30000;
	private Integer connectionTimeout = 30000;
	private Boolean isSingleton = true;
	
	
	@Bean
	public CuratorFramework zookeeper(){
		return CuratorFrameworkFactory
									.builder()
									.connectString(String.format("%s:%s", this.zkHost,this.zkPort))
									.sessionTimeoutMs(this.sessionTimeout)
									.connectionTimeoutMs(this.connectionTimeout)
									.canBeReadOnly(false)
									.retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
									.namespace("rpc" + "/" + this.namespace)
									.defaultData(null)
									.build();
	}
	
	@Bean
	public ZookeeperFactory thriftZookeeperFactory(){
		ZookeeperFactory thriftZookeeperFactory = new ZookeeperFactory();
		thriftZookeeperFactory.setHost(String.format("%s:%s", this.zkHost,this.zkPort));
		thriftZookeeperFactory.setNamespace(this.namespace);
		thriftZookeeperFactory.setSessiontimeout(this.sessionTimeout);
		thriftZookeeperFactory.setConnectiontimeout(this.connectionTimeout);
		thriftZookeeperFactory.setSingleton(this.isSingleton);
		return thriftZookeeperFactory;
	}
	
	@Bean
	public ThriftServerAddressProvider thriftServerAddressProviderZookeeper() throws Exception{
		ThriftServerAddressProviderZookeeper thriftServerAddressProviderZookeeper = new ThriftServerAddressProviderZookeeper();
		thriftServerAddressProviderZookeeper.setService("com.duowan.niejin.thrift.UserService");
		thriftServerAddressProviderZookeeper.setVersion("1.0.0");
		thriftServerAddressProviderZookeeper.setZookeeperFactory(this.thriftZookeeperFactory());
		thriftServerAddressProviderZookeeper.setServiceBalance(new RandomStrategy());
		return thriftServerAddressProviderZookeeper;
	}
	
	@Bean
	public ThriftServiceClientProxyFactory thriftServiceProxy() throws Exception{
		//CREATA ClientProxy
		ThriftServiceClientProxyFactory thriftServiceProxy = new ThriftServiceClientProxyFactory();
		thriftServiceProxy.setIdleTime(1800000);
		thriftServiceProxy.setMaxActive(5);
		thriftServiceProxy.setServerAddressProvider(this.thriftServerAddressProviderZookeeper());
		return thriftServiceProxy;
	}
}
