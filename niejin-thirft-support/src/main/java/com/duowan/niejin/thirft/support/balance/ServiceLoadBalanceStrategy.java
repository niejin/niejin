package com.duowan.niejin.thirft.support.balance;

import java.util.HashMap;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月13日
 *
**/
public interface ServiceLoadBalanceStrategy {
	String select(HashMap<String, Integer> serverMap);
}
