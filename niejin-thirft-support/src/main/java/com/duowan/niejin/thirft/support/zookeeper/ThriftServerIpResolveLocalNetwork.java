package com.duowan.niejin.thirft.support.zookeeper;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月7日
 *
 **/
public class ThriftServerIpResolveLocalNetwork implements ThriftServerIpResolve {

	private static final Logger logger = LoggerFactory.getLogger(ThriftServerIpResolveLocalNetwork.class);

	// cache server ip
	private String serverIp;

	@Override
	public String getServerIp() throws Exception {
		if (serverIp != null) {
			return serverIp;
		}

		serverIp = this.parserLocalIpNetwork();

		if (serverIp == null) {
			throw new Exception("cannot resolve LocalIpNetwork ...");
		}
		return serverIp;
	}

	@Override
	public void reset() {
		serverIp = null;
	}

	// 一个主机有多个网络接口
	private String parserLocalIpNetwork() {
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();

			while (netInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = netInterfaces.nextElement();
				// 每个网络接口,都会有多个"网络地址",比如一定会有lookback地址,会有siteLocal地址等.以及IPV4或者IPV6
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address instanceof Inet6Address) {
						continue;
					}
					if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
						logger.info("resolve rpc ip : {}", serverIp);
						return address.getHostAddress();
					}
				}

			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		ThriftServerIpResolveLocalNetwork resolve = new ThriftServerIpResolveLocalNetwork();
		System.out.println(resolve.getServerIp());
	}
}
