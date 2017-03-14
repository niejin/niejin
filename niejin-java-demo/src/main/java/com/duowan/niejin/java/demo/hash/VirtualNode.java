package com.duowan.niejin.java.demo.hash;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月13日
 *
**/
public class VirtualNode {
	
	private int replicaNumber;
	private PhysicalNode parent;
	
	public VirtualNode(PhysicalNode parentNode,int replicaNumber){
		this.parent = parentNode;
		this.replicaNumber = replicaNumber;
	}
	
	public boolean matchs(String host){
		return parent.toString().equalsIgnoreCase(host);
	}
	
	@Override
	public String toString(){
		return parent.toString().toLowerCase() + ":" + replicaNumber;
	}

	public int getReplicaNumber() {
		return replicaNumber;
	}

	public void setReplicaNumber(int replicaNumber) {
		this.replicaNumber = replicaNumber;
	}

	public PhysicalNode getParent() {
		return parent;
	}

	public void setParent(PhysicalNode parent) {
		this.parent = parent;
	}
}
