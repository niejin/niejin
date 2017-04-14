package com.duowan.niejin.thirft.support;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月6日
 *
 **/
public class ThriftServiceServer extends Thread{

	private TServer server;

	public ThriftServiceServer(TProcessor processor, Integer port) throws TTransportException {
		initSingleService(processor, port);
	}

	public ThriftServiceServer(Map<String, TProcessor> processors, Integer port) throws TTransportException {
		initMultiService(processors, port);
	}

	private void initSingleService(TProcessor processor, Integer port) throws TTransportException {
		// NonBlocking
		TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
		//
		TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);

		TProcessorFactory processorFactory = new TProcessorFactory(processor);
		tArgs.processorFactory(processorFactory);
		tArgs.transportFactory(new TFramedTransport.Factory());
		tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
		server = new TThreadedSelectorServer(tArgs);
	}

	private void initMultiService(Map<String, TProcessor> processorMap, Integer port) throws TTransportException {
		// NonBlocking
		TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
		//
		TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
		// 多路复用
		TMultiplexedProcessor multiplexedProccessor = new TMultiplexedProcessor();
		Iterator<Entry<String, TProcessor>> entry = processorMap.entrySet().iterator();
		while (entry.hasNext()) {
			Entry<String, TProcessor> kv = entry.next();
			multiplexedProccessor.registerProcessor(kv.getKey(), kv.getValue());
		}

		TProcessorFactory processorFactory = new TProcessorFactory(multiplexedProccessor);
		tArgs.processorFactory(processorFactory);
		tArgs.transportFactory(new TFramedTransport.Factory());
		tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
		server = new TThreadedSelectorServer(tArgs);
	}

	@Override
	public void run() {
		try {
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
