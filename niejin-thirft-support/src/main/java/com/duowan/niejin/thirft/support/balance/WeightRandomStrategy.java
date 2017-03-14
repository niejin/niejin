package com.duowan.niejin.thirft.support.balance;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月13日
 *
 **/
public class WeightRandomStrategy implements ServiceLoadBalanceStrategy {

	@Override
	public InetSocketAddress select(List<InetSocketAddress> servers){
		throw new NotImplementedException("undo");
		/*if (servers == null || servers.isEmpty())
			return null;

		Set<String> keys = serverMap.keySet();
		Iterator<String> it = keys.iterator();
		ArrayList<String> keysList = new ArrayList<String>();

		while (it.hasNext()) {
			String serverAddress = it.next();
			int weight = serverMap.get(serverAddress);
			for (int i = 0; i < weight; i++) {
				keysList.add(serverAddress);
			}
		}

		java.util.Random random = new java.util.Random();
		int randomPos = random.nextInt(keysList.size());

		return keysList.get(randomPos);*/
	}

}
