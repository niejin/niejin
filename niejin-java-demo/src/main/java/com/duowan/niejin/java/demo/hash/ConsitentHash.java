package com.duowan.niejin.java.demo.hash;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月13日
 *
 **/
public class ConsitentHash {
	private SortedMap<Long, VirtualNode> ring = new TreeMap<Long, VirtualNode>();
	private MD5Hash md5HashFunction = new MD5Hash();

	public ConsitentHash(Collection<PhysicalNode> pNodes, int vNodeCount) {
		for (PhysicalNode pNode : pNodes) {
			this.addNode(pNode, vNodeCount);
		}
	}

	public void addNode(PhysicalNode pNode, int vNodeCount) {
		int existingReplicas = this.getReplicas(pNode.toString());
		for (int i = 0; i < vNodeCount; i++) {
			VirtualNode vNode = new VirtualNode(pNode, i + existingReplicas);
			ring.put(md5HashFunction.hash(vNode.toString()), vNode);
		}
	}

	public void removeNode(PhysicalNode pNode) {
		Iterator<Long> it = ring.keySet().iterator();
		while (it.hasNext()) {
			Long key = it.next();
			VirtualNode virtualNode = ring.get(key);
			if (virtualNode.matchs(pNode.toString())) {
				it.remove();
			}
		}
	}

	public PhysicalNode getNode(String key) {
		if (ring.isEmpty()) {
			return null;
		}

		Long hashKey = md5HashFunction.hash(key);
		SortedMap<Long, VirtualNode> tailMap = ring.tailMap(hashKey);
		hashKey = tailMap != null && !tailMap.isEmpty() ? tailMap.firstKey() : ring.firstKey();
		return ring.get(hashKey).getParent();

	}

	public int getReplicas(String nodeName) {
		int replicas = 0;
		for (VirtualNode node : ring.values()) {
			if (node.matchs(nodeName)) {
				replicas++;
			}
		}
		return replicas;
	}

	private static class MD5Hash {
		MessageDigest md5;

		public MD5Hash() {
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Long hash(String key) {
			md5.reset();
			md5.update(key.getBytes());
			byte[] digest = md5.digest();

			Long h = 0l;
			for (int i = 0; i < 4; i++) {
				h <<= 8;
				h |= ((int) digest[i]) & 0xFF;
			}
			return h;
		}
	}
}
