package com.duowan.niejin.thirft.support.balance;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月13日
 *
**/
public class RoundRobinStrategy implements ServiceLoadBalanceStrategy {

    private static Integer pos = 0;
    
	@Override
	public InetSocketAddress select(List<InetSocketAddress> servers){
		if(servers == null || servers.isEmpty()) return null;
		
		InetSocketAddress server = null;
		synchronized (pos) {
			if(pos > servers.size())
				pos = 0;
			server = servers.get(pos);
			pos ++ ;
		}
		return server;
	}

}
