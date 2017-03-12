package com.duowan.niejin.thirft.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.annotation.PostConstruct;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressProvider;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月8日
 * @version 客户端代理工厂
 **/
public class ThriftServiceClientProxyFactory implements FactoryBean {

	private static final Logger logger = LoggerFactory.getLogger(ThriftServiceClientProxyFactory.class);

	private Integer maxActive = 32;// 最大活跃连接数

	private Integer idleTime = 180000;// ms default 3min 链接空间时间 -1： 关闭空闲检测

	private ThriftServerAddressProvider serverAddressProvider;

	private Object proxyClient;

	private Class<?> objectClass;

	private GenericObjectPool<TServiceClient> pool;

	private PoolOperationCallBack callback = new PoolOperationCallBack() {
		@Override
		public void make(TServiceClient client) {
			logger.info("create");
		}

		@Override
		public void destroy(TServiceClient client) {
			logger.info("destroy");
		}
	};

	@PostConstruct
	public void init() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			// 加载Iface接口
			objectClass = classLoader.loadClass(serverAddressProvider.getService() + "$Iface");
			// 加载client.Factory类
			Class<TServiceClientFactory<TServiceClient>> factoryClzz = (Class<TServiceClientFactory<TServiceClient>>) classLoader
					.loadClass(serverAddressProvider.getService() + "$Client$Factory");

			TServiceClientFactory<TServiceClient> clientPool = factoryClzz.newInstance();

			ThriftClientPoolFactory clientPoolFactory = new ThriftClientPoolFactory(serverAddressProvider, clientPool,
					callback);

			pool = new GenericObjectPool<TServiceClient>(clientPoolFactory, makePoolConfig());

			// InvocationHandler handler = makerProxyHandler();
			InvocationHandler handler = makerProxyHandler2();

			proxyClient = Proxy.newProxyInstance(classLoader, new Class[] { objectClass }, handler);
		} catch (ClassNotFoundException e) {
			logger.info("ThriftServeiceClientProxyFactory init failed!");
			logger.error(e.getMessage(), e);
		} catch (InstantiationException e) {
			logger.info("ThriftServeiceClientProxyFactory init failed!");
			logger.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.info("ThriftServeiceClientProxyFactory init failed!");
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.info("ThriftServeiceClientProxyFactory init failed!");
			logger.error(e.getMessage(), e);
		}
	}

	private InvocationHandler makerProxyHandler() throws Exception {
		ThriftServiceClient2Proxy handler = null;
		TServiceClient client = null;
		try {
			client = pool.borrowObject();
			handler = new ThriftServiceClient2Proxy(client);
			pool.returnObject(client);
		} catch (Exception e) {
			pool.invalidateObject(client);
			throw new ThriftException("获取代理对象失败");
		}
		return handler;
	}

	private InvocationHandler makerProxyHandler2() {
		ThriftServiceClientProxy handler = new ThriftServiceClientProxy(pool);
		return handler;
	}

	private Config makePoolConfig() {
		GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
		poolConfig.maxActive = maxActive;
		poolConfig.maxIdle = 1;
		poolConfig.minIdle = 0;
		poolConfig.minEvictableIdleTimeMillis = idleTime;
		poolConfig.timeBetweenEvictionRunsMillis = idleTime * 2L;
		poolConfig.testOnBorrow = true;
		poolConfig.testOnReturn = false;
		poolConfig.testWhileIdle = false;
		return poolConfig;
	}

	public void close(){
		if(pool != null){
			try{
				pool.close();
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
		}
		
		if(serverAddressProvider != null){
			serverAddressProvider.close();
		}
	}
	
	public Integer getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public Integer getIdleTime() {
		return idleTime;
	}

	public void setIdleTime(Integer idleTime) {
		this.idleTime = idleTime;
	}

	public ThriftServerAddressProvider getServerAddressProvider() {
		return serverAddressProvider;
	}

	public void setServerAddressProvider(ThriftServerAddressProvider serverAddressProvider) {
		this.serverAddressProvider = serverAddressProvider;
	}

	@Override
	public Object getObject() throws Exception {
		return proxyClient;
	}

	@Override
	public Class getObjectType() {
		return objectClass;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
