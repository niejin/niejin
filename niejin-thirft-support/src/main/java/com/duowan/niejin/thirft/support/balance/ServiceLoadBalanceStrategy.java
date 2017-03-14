package com.duowan.niejin.thirft.support.balance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月13日
 *
**/
public interface ServiceLoadBalanceStrategy {
	InetSocketAddress select(List<InetSocketAddress> servers);
}
