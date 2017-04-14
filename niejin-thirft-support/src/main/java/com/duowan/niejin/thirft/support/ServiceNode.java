package com.duowan.niejin.thirft.support;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月6日
 *
 **/
public class ServiceNode {

	private Object service;
	private Integer weight;
	private String version;
	private String thriftServiceName;
	
	public Object getService() {
		return service;
	}

	public void setService(Object service) {
		this.service = service;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getThriftServiceName() {
		return thriftServiceName;
	}

	public void setThriftServiceName(String thriftServiceName) {
		this.thriftServiceName = thriftServiceName;
	}
}
