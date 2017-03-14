package com.duowan.niejin.thirft.support.balance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月13日
 *
 **/
public class WeightRoundRobinStrategy implements ServiceLoadBalanceStrategy {
	private static Integer pos;

	@Override
	public String select(HashMap<String, Integer> serverMap) {
		if (serverMap == null || serverMap.isEmpty())
			return null;

		Set<String> keys = serverMap.keySet();
		Iterator<String> it = keys.iterator();

		ArrayList<String> keysList = new ArrayList<String>();
		while (it.hasNext()) {
			String serverAddress = it.next();
			Integer weight = serverMap.get(serverAddress);
			for (int i = 0; i < weight; i++) {
				keysList.add(serverAddress);
			}
		}

		String server = null;
		synchronized (pos) {
			if (pos > keysList.size())
				pos = 0;
			server = keysList.get(pos);
			pos++;
		}
		return server;
	}

}
