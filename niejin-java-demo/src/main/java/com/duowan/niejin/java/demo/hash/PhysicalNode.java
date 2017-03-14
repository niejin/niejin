package com.duowan.niejin.java.demo.hash;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月13日
 *
**/
public class PhysicalNode {
	private String domain;
	private String ip;
	private Integer port;
	
	public PhysicalNode(String domain,String ip ,Integer port){
		this.domain = domain;
		this.ip = ip;
		this.port = port;
	}
	
	@Override
	public String toString(){
		return this.domain + ":" + this.ip + ":" + this.port;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
}
