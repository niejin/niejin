package com.duowan.niejin.thirft.support.balance;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月13日
 *
 **/
public class RandomStrategy implements ServiceLoadBalanceStrategy {

	@Override
	public InetSocketAddress select(List<InetSocketAddress> servers){
		if (servers == null || servers.isEmpty())
			return null;

		java.util.Random random = new java.util.Random();
		int randomPos = random.nextInt(servers.size());
		return servers.get(randomPos);
	}

}
