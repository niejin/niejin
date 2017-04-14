package com.duowan.niejin.thirft.support;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.springframework.beans.factory.InitializingBean;

import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressRegister;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerIpResolve;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerIpResolveLocalNetwork;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月6日
 *
 **/
public class ThriftServiceMultiServerFactory implements InitializingBean {

	private Integer port = 9090;

	private List<ServiceNode> services;

	// 服务注册
	private ThriftServerAddressRegister thriftServerAddressRegister;

	// 解析本机IP
	private ThriftServerIpResolve thriftServerIpResolve;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.init();
	}

	private void init() throws Exception {
		if (thriftServerIpResolve == null) {
			thriftServerIpResolve = new ThriftServerIpResolveLocalNetwork();
		}

		String serverIp = thriftServerIpResolve.getServerIp();
		if (StringUtils.isBlank(serverIp)) {
			throw new ThriftException("cannot find rpc ip.");
		}

		String localeHost = serverIp + ":" + port;

		Map<String, TProcessor> processors = new HashMap<String, TProcessor>();
		for (ServiceNode node : services) {
			try {
				Class<?> serviceClass = node.getService().getClass();
				Class<?>[] interfaces = serviceClass.getInterfaces();

				if (interfaces.length == 0) {
					throw new IllegalClassFormatException("api-class should implements Iface");
				}

				TProcessor processor = null;
				String serviceName = null;
				for (Class<?> clazz : interfaces) {
					String interfaceName = clazz.getSimpleName();
					if (!interfaceName.equals("Iface")) {
						continue;
					}

					serviceName = clazz.getEnclosingClass().getName();
					String serviceProcessorClassName = serviceName + "$Processor";
					try {
						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
						Class<?> processorClazz = classLoader.loadClass(serviceProcessorClassName);
						if(!TProcessor.class.isAssignableFrom(processorClazz)){
							continue;
						}
						
						Constructor<?> constructor = processorClazz.getConstructor(clazz);
						processor = (TProcessor) constructor.newInstance(node.getService());
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if(processor != null){
					processors.put(serviceName, processor);
					node.setThriftServiceName(serviceName);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		new ThriftServiceServer(processors, port).start();
		// 注册服务
		if (thriftServerAddressRegister != null) {
			for (ServiceNode node : services) {
				thriftServerAddressRegister.register(node.getThriftServiceName(),
						node.getVersion(), localeHost);
			}
		}
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public List<ServiceNode> getServices() {
		return services;
	}

	public void setServices(List<ServiceNode> services) {
		this.services = services;
	}

	public ThriftServerAddressRegister getThriftServerAddressRegister() {
		return thriftServerAddressRegister;
	}

	public void setThriftServerAddressRegister(ThriftServerAddressRegister thriftServerAddressRegister) {
		this.thriftServerAddressRegister = thriftServerAddressRegister;
	}

	public ThriftServerIpResolve getThriftServerIpResolve() {
		return thriftServerIpResolve;
	}

	public void setThriftServerIpResolve(ThriftServerIpResolve thriftServerIpResolve) {
		this.thriftServerIpResolve = thriftServerIpResolve;
	}
}
