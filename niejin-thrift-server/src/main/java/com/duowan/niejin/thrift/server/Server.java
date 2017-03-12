package com.duowan.niejin.thrift.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月6日
 *
 **/
public class Server {

	private static final Integer PORT = 9090;

	// 簡單的單線程服務模型，一般用戶測試
	public void startSimpleServer() throws TTransportException {
		// TODO ...
		TProcessor processor = null;

		TServerSocket serverTransport = new TServerSocket(PORT);
		TServer.Args args = new TServer.Args(serverTransport);

		args.processor(processor);
		args.protocolFactory(new TBinaryProtocol.Factory());

		TServer server = new TSimpleServer(args);
		server.serve();
	}

	//线程池服务模型，使用标准的阻塞式IO，预先创建一组线程处理请求。
	public void startThreadPoolServer() throws TTransportException {
		// TODO ...
		TProcessor processor = null;
		
		TServerSocket serverTransport = new TServerSocket(PORT);
		TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
		
		args.processor(processor);
		args.protocolFactory(new TBinaryProtocol.Factory());
		
		//线程池模型 使用标准的阻塞IO,预先创建一组线程处理请求
		TServer server = new TThreadPoolServer(args);
		server.serve();
	}
	
	// 线程池服务模型，使用标准的阻塞式IO，使用非阻塞式IO
	public void startNonblockingServer() throws TTransportException{
		// TODO ...
		TProcessor processor = null;
		
		TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(PORT);
		TNonblockingServer.Args args = new TNonblockingServer.Args(serverTransport);
		
		args.processor(processor);
		// 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
		args.transportFactory(new TFramedTransport.Factory());
		args.protocolFactory(new TBinaryProtocol.Factory());
		
		
		//线程池模型 使用标准的阻塞IO,预先创建一组线程处理请求
		TServer server = new TNonblockingServer(args);
		server.serve();
	}
	
	//半同步半异步的读服务端模型,需要指定为TFramedTransport数据传输的方式
	public void statHsHaServer() throws TTransportException{
		//TODO init Proccessor
		TProcessor processor = null;
		
		TNonblockingServerSocket  serverTransport = new TNonblockingServerSocket(PORT);
		THsHaServer.Args args = new THsHaServer.Args(serverTransport);
		
		args.processor(processor);
		// 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
		args.transportFactory(new TFramedTransport.Factory());
		args.protocolFactory(new TBinaryProtocol.Factory());
		
		
		//线程池模型 使用标准的阻塞IO,预先创建一组线程处理请求
		TServer server = new THsHaServer(args);
		server.serve();
	}

}
