package com.duowan.niejin.thirft.support;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressRegister;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerIpResolve;
import com.duowan.niejin.thirft.support.zookeeper.ThriftServerIpResolveLocalNetwork;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月8日
 * @version 服务端工厂类
 **/
public class ThriftServiceServerFactory implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ThriftServiceServerFactory.class);

	// 服务注册本机端口
	private Integer port = 8299;
	// 优先级
	private Integer weight = 1;// default
	// 服务实现类
	private Object service;// serice实现类
	// 服务版本号
	private String version;
	// 服务注册
	private ThriftServerAddressRegister thriftServerAddressRegister;

	// 解析本机IP
	private ThriftServerIpResolve thriftServerIpResolve;

	private ServerThread serverThread;

	public void setPort(Integer port) {
        this.port = port;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setThriftServerAddressRegister(ThriftServerAddressRegister thriftServerAddressRegister){
        this.thriftServerAddressRegister = thriftServerAddressRegister;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	this.init();
    }
    
	public void init() throws Exception{
		if(thriftServerIpResolve == null){
			thriftServerIpResolve = new ThriftServerIpResolveLocalNetwork();
		}
		
		String serverIp = thriftServerIpResolve.getServerIp();
		if(StringUtils.isBlank(serverIp)){
			throw new ThriftException("cannot find rpc ip.");
		}
		
		String hostName = serverIp + ":" + port + ":" + weight;
		
		Class<?> serviceClass = service.getClass();
		
		//获取实现类接口
		Class<?>[] interfaces = serviceClass.getInterfaces();
		
		if(interfaces.length == 0){
			throw new IllegalClassFormatException("api-class should implements Iface");
		}
		
		//reflect load "Processor"
		TProcessor processor = null;
		String serviceName = null;
		for(Class<?> clazz : interfaces){
			String cname = clazz.getSimpleName();
			if(!cname.equals("Iface")){
				continue;
			}
			
			serviceName = clazz.getEnclosingClass().getName();
			String pname = serviceName + "$Processor";
			try{
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				Class<?> pclass = classLoader.loadClass(pname);
				if(!TProcessor.class.isAssignableFrom(pclass)){
					continue;
				}
				Constructor<?> constructor = pclass.getConstructor(clazz);
				processor = (TProcessor) constructor.newInstance(service);
				break;
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
		}
		
		if(processor == null){
			throw new IllegalClassFormatException("api-class should implements Iface");
		}
		
		//需要单独的线程,因为serve方法是阻塞的.
		serverThread = new ServerThread(processor, port);
		serverThread.start();
		//注册服务
		if(thriftServerAddressRegister != null){
			thriftServerAddressRegister.register(serviceName, version, hostName);
		}
		
	}
    
	//服务端启动线程
	class ServerThread extends Thread {

		private TServer server;

		public ServerThread(TProcessor processor, Integer port) throws TTransportException {
			initServer(processor, port);
		}

		private void initServer(TProcessor processor, Integer port) throws TTransportException {
			//NonBlocking
			TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
			//
			TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
			
			TProcessorFactory processorFactory = new TProcessorFactory(processor);
			//多路复用
			/*TMultiplexedProcessor multiplexedProccessor = new TMultiplexedProcessor();
			multiplexedProccessor.registerProcessor("server1", server1_processor);*/
			
			tArgs.processorFactory(processorFactory);
			tArgs.transportFactory(new TFramedTransport.Factory());
			tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
			server = new TThreadedSelectorServer(tArgs);
		}

		@Override
		public void run() {
			try {
				// 启动服务
				server.serve();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		public void stopServer() {
			if (server != null) {
				server.stop();
			}
		}
	}
	
	public void close() {
        serverThread.stopServer();
    }
}
