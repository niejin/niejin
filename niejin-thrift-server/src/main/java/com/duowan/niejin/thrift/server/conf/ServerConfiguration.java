package com.duowan.niejin.thrift.server.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duowan.niejin.thirft.support.ThriftServiceServerFactory;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressRegisterZookeeper;
import com.duowan.niejin.thirft.support.zookeeper.ZookeeperFactory;
import com.duowan.niejin.thrift.UserService;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月11日
 *
**/
@Configuration
public class ServerConfiguration {
	//zookeeper.server.list=172.17.5.181\:2181
	private String zkHost = "172.17.5.181";
	private Integer zkPort = 2181;
	private String namespace = "thrift.rpc";
	private Integer sessionTimeout = 30000;
	private boolean isSingleton = false;
	
	@Autowired
	UserService.Iface userService;
	
	@Bean
	public ZookeeperFactory thriftZookeeperFactory(){
		ZookeeperFactory thriftZookeeperFactory = new ZookeeperFactory();
		thriftZookeeperFactory.setHost(String.format("%s:%s", this.zkHost,this.zkPort));
		thriftZookeeperFactory.setNamespace(this.namespace);
		thriftZookeeperFactory.setSessiontimeout(this.sessionTimeout);
		thriftZookeeperFactory.setSingleton(this.isSingleton);
		return thriftZookeeperFactory;
	}
	
	@Bean
	public ThriftServerAddressRegisterZookeeper thriftServiceAddressRegister() throws Exception{
		ThriftServerAddressRegisterZookeeper thriftServiceAddressRegister =  new ThriftServerAddressRegisterZookeeper();
		thriftServiceAddressRegister.setZkClient(this.thriftZookeeperFactory().getObject());
		return thriftServiceAddressRegister;
	}
	
	@Bean
	public ThriftServiceServerFactory registerUserService() throws Exception{
		ThriftServiceServerFactory userServiceRegister =  new ThriftServiceServerFactory();
		userServiceRegister.setService(userService);
		userServiceRegister.setPort(9090);
		userServiceRegister.setVersion("1.0.0");
		userServiceRegister.setWeight(1);
		userServiceRegister.setThriftServerAddressRegister(this.thriftServiceAddressRegister());
		return userServiceRegister;
	}
}
