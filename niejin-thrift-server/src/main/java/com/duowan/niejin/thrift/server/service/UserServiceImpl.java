package com.duowan.niejin.thrift.server.service;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.duowan.niejin.thrift.User;
import com.duowan.niejin.thrift.UserService;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年2月20日
 *
 **/
@Component
public class UserServiceImpl implements UserService.Iface {
	@Override
	public User getUser(int id) throws TException {
		User user = new User();
		
		user.setId(1);
		user.setName("niejin");
		
		return user;		
	}
}
