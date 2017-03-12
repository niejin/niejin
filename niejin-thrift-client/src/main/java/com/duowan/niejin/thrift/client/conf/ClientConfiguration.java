package com.duowan.niejin.thrift.client.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duowan.niejin.thirft.support.ThriftServiceClientProxyFactory;
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
	public ZookeeperFactory thriftZookeeperFactory(){
		ZookeeperFactory thriftZookeeperFactory = new ZookeeperFactory();
		thriftZookeeperFactory.setHost(String.format("%s:%s", this.zkHost,this.zkPort));
		thriftZookeeperFactory.setNamespace(this.namespace);
		thriftZookeeperFactory.setSessiontimeout(this.sessionTimeout);
		thriftZookeeperFactory.setConnectiontimeout(this.connectionTimeout);
		thriftZookeeperFactory.setSingleton(this.isSingleton);
		return thriftZookeeperFactory;
	}
	
	/*@Bean
	public ThriftServerAddressProvider thriftServerAddressProviderZookeeper() throws Exception{
		ThriftServerAddressProviderZookeeper thriftServerAddressProviderZookeeper = new ThriftServerAddressProviderZookeeper();
		thriftServerAddressProviderZookeeper.setService("com.duowan.niejin.thrift.UserService");
		thriftServerAddressProviderZookeeper.setVersion("1.0.0");
		thriftServerAddressProviderZookeeper.setZkClient(this.thriftZookeeperFactory().getObject());
		return thriftServerAddressProviderZookeeper;
	}*/
	
	@Bean
	public ThriftServiceClientProxyFactory thriftServiceProxy() throws Exception{
		//CREATE ThriftServerAddressProviderZookeeper
		ThriftServerAddressProviderZookeeper thriftServerAddressProviderZookeeper = new ThriftServerAddressProviderZookeeper();
		thriftServerAddressProviderZookeeper.setService("com.duowan.niejin.thrift.UserService");
		thriftServerAddressProviderZookeeper.setVersion("1.0.0");
		thriftServerAddressProviderZookeeper.setZkClient(this.thriftZookeeperFactory().getObject());
		//CREATA ClientProxy
		ThriftServiceClientProxyFactory thriftServiceProxy = new ThriftServiceClientProxyFactory();
		thriftServiceProxy.setIdleTime(1800000);
		thriftServiceProxy.setMaxActive(5);
		thriftServiceProxy.setServerAddressProvider(thriftServerAddressProviderZookeeper);
		
		return thriftServiceProxy;
	}
}
