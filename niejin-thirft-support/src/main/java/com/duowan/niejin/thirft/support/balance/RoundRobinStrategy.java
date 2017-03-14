package com.duowan.niejin.thirft.support.balance;

import java.util.ArrayList;
import java.util.HashMap;
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
	public String select(HashMap<String, Integer> serverMap) {
		if(serverMap == null || serverMap.isEmpty()) return null;
		
		Set<String> keys = serverMap.keySet();
		ArrayList<String> keysList = new ArrayList<String>();
		keysList.addAll(keys);
		
		String server = null;
		
		synchronized (pos) {
			if(pos > keys.size())
				pos = 0;
			server = keysList.get(pos);
			pos ++ ;
		}
		return server;
	}

}
