package com.duowan.niejin.zookeeper.demo.discovery;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月15日
 *
**/
public class ServerPayload {
	private String cluster;
    private int payload;

    public ServerPayload(){

    }

    public ServerPayload(String cluster, int payload) {
        this.cluster = cluster;
        this.payload = payload;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public int getPayload() {
        return payload;
    }

    public void setPayload(int payload) {
        this.payload = payload;
    }
}
