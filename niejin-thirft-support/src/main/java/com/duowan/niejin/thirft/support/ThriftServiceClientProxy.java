package com.duowan.niejin.thirft.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TServiceClient;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月7日 客户端代理类
 **/
public class ThriftServiceClientProxy implements InvocationHandler {

	private GenericObjectPool<TServiceClient> pool;

	private TServiceClient client;

	public ThriftServiceClientProxy(TServiceClient client) {
		this.client = client;
	}
	
	public ThriftServiceClientProxy(GenericObjectPool<TServiceClient> pool) {
		this.pool = pool;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		TServiceClient client = pool.borrowObject();
		boolean flag = true;
		try {
			return method.invoke(client, args);
		} catch (Exception e) {
			flag = false;
			throw e;
		} finally {
			if (flag) {
				pool.returnObject(client);
			} else {
				pool.invalidateObject(client);
			}
		}
	}

}
