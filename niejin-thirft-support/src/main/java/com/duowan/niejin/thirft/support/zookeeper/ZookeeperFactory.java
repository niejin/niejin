package com.duowan.niejin.thirft.support.zookeeper;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.FactoryBean;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月6日
 * 获取zookeeper客户端链接
**/
public class ZookeeperFactory implements FactoryBean<CuratorFramework> {
	
	private static final String ROOT = "rpc";
	
	private String host = "localhost:9090";
	private Integer sessiontimeout = 30000;
	private Integer connectiontimeout = 30000;
	//全局path前缀,常用来区分不同的服务
	private String namespace;
	//共享一个zk连接
	private boolean singleton = true;
	
	private CuratorFramework zkClient;
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getSessiontimeout() {
		return sessiontimeout;
	}

	public void setSessiontimeout(Integer sessiontimeout) {
		this.sessiontimeout = sessiontimeout;
	}

	public Integer getConnectiontimeout() {
		return connectiontimeout;
	}

	public void setConnectiontimeout(Integer connectiontimeout) {
		this.connectiontimeout = connectiontimeout;
	}

	public CuratorFramework getZkClient() {
		return zkClient;
	}

	public void setZkClient(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public CuratorFramework getObject() throws Exception {
		if(isSingleton()){
			synchronized (ZookeeperFactory.class) {
				CuratorFramework tmp = zkClient;
				if(tmp == null){
					tmp = create();
					zkClient = tmp;
				}
			}
		}
		return create();
	}

	@Override
	public Class<?> getObjectType() {
		 return CuratorFramework.class;
	}

	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	private CuratorFramework create(){
		if(StringUtils.isBlank(namespace)){
			namespace = this.ROOT;
		}else{
			namespace = this.ROOT + "/" + namespace;
		}
		return create(host,sessiontimeout,connectiontimeout,namespace);
	}
	
	private CuratorFramework create(String host,Integer sessiontimeout,Integer connectiontimeout,String namespace){
		return CuratorFrameworkFactory.builder()
				.connectString(host)
				.sessionTimeoutMs(sessiontimeout)
				.connectionTimeoutMs(connectiontimeout)
				.canBeReadOnly(true)
				.namespace(namespace)
				.retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
				.defaultData(null)
				.build();
	}
	
	public void close(){
		if(null != zkClient){
			zkClient.close();
		}
	}
}
