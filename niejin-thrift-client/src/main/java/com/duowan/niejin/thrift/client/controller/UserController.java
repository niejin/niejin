package com.duowan.niejin.thrift.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.duowan.niejin.thirft.support.ThriftServiceClientProxyFactory;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressProvider;
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
	ThriftServiceClientProxyFactory thriftServiceProxy;
	
	@ResponseBody
	@RequestMapping(value = "/hello")
	String hello() throws Exception {
		
		
		UserService.Iface userService = (UserService.Iface) thriftServiceProxy.getObject();
		User user = userService.getUser(1);
		//System.out.println(user.getId() + ":" + user.getName());
		return String.format("Hello,%s[%s]", user.getName(),user.getId());
	}
}
