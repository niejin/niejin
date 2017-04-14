package com.duowan.niejin.thirft.support;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
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
		
		String hostName = serverIp + ":" + port;
		
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
		
		public ServerThread(Map<String,TProcessor> processors, Integer port) throws TTransportException {
			initServer2(processors, port);
		}

		private void initServer(TProcessor processor, Integer port) throws TTransportException {
			//NonBlocking
			TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
			//
			TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
			
			TProcessorFactory processorFactory = new TProcessorFactory(processor);
			tArgs.processorFactory(processorFactory);
			tArgs.transportFactory(new TFramedTransport.Factory());
			tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
			server = new TThreadedSelectorServer(tArgs);
		}

		private void initServer2(Map<String,TProcessor> processorMap, Integer port) throws TTransportException {
			//NonBlocking
			TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
			//
			TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
			//多路复用
			TMultiplexedProcessor multiplexedProccessor = new TMultiplexedProcessor();
			Iterator<Entry<String, TProcessor>> entry = processorMap.entrySet().iterator();
			while(entry.hasNext()){
				Entry<String, TProcessor> kv = entry.next();
				multiplexedProccessor.registerProcessor(kv.getKey(), kv.getValue());
			}
			
			TProcessorFactory processorFactory = new TProcessorFactory(multiplexedProccessor);
			tArgs.processorFactory(processorFactory);
			tArgs.transportFactory(new TFramedTransport.Factory());
			tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
			server = new TThreadedSelectorServer(tArgs);
			//server = new TNonblockingServer(tArgs);
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
