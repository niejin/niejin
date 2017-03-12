package com.duowan.niejin.thrift.client.controller;

import javax.annotation.PostConstruct;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.duowan.niejin.thirft.support.ThriftServiceClientProxyFactory;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressProvider;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressProviderZookeeper;
import com.duowan.niejin.thirft.support.zookeeper.ZookeeperFactory;
import com.duowan.niejin.thrift.User;
import com.duowan.niejin.thrift.UserService;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年2月20日
 *
**/
@Controller
public class UserController {

	@Autowired
	ThriftServerAddressProvider thriftServerAddressProviderZookeeper;
	
	@Autowired
	ThriftServiceClientProxyFactory thriftServiceProxy;
	
	@ResponseBody
	@RequestMapping(value = "/hello")
	String hello() throws Exception {
		Long startTimestamp = System.currentTimeMillis();
		for(int i = 0;i<1;i++){
			UserService.Iface userService = (UserService.Iface) thriftServiceProxy.getObject();
			User user = userService.getUser(1);
		}
		Long endTimestamp = System.currentTimeMillis();
		System.out.println("Cost all times : " + (endTimestamp - startTimestamp));
		//System.out.println(user.getId() + ":" + user.getName());
		//return String.format("%s:%s", user.getId(),user.getName());
		return "Cost all times : " + (endTimestamp - startTimestamp);
	}
}
