package com.duowan.niejin.thirft.support;

import java.net.InetSocketAddress;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duowan.niejin.thirft.support.zookeeper.ThriftServerAddressProvider;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月7日
 *
 **/
public class ThriftClientPoolFactory extends BasePoolableObjectFactory<TServiceClient> {

	private static final Logger logger = LoggerFactory.getLogger(ThriftClientPoolFactory.class);
	
	private ThriftServerAddressProvider serverAddressProvider;
	private TServiceClientFactory<TServiceClient> clientFactory;
	private PoolOperationCallBack callback;
	
	public ThriftClientPoolFactory(ThriftServerAddressProvider addressProvider, TServiceClientFactory<TServiceClient> client){
		this.serverAddressProvider = addressProvider;
		this.clientFactory = client;
	}
	
	public ThriftClientPoolFactory(ThriftServerAddressProvider addressProvider, TServiceClientFactory<TServiceClient> client,PoolOperationCallBack callback) {
		this(addressProvider, client);
		this.callback = callback;
	}
	
	
	
	@Override
	public TServiceClient makeObject() throws Exception {
		InetSocketAddress address = serverAddressProvider.selector();
		if(address == null){
			throw new ThriftException("No provider available for remote api");
		}
		
		TSocket socket = new TSocket(address.getHostName(),address.getPort());
		TTransport transport = new TFramedTransport(socket);
		TProtocol protocol = new TBinaryProtocol(transport);
		TServiceClient client = this.clientFactory.getClient(protocol);
		transport.open();
		
		if(callback != null){
			try{
				callback.make(client);
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
		}
		return client;
	}

	@Override
	public void destroyObject(TServiceClient client) throws Exception {
		if(callback != null){
			try{
				callback.destroy(client);
			}catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		
		logger.info("destroy Object : {} ",client);
		
		TTransport inTransport = client.getInputProtocol().getTransport();
		TTransport outTransport = client.getOutputProtocol().getTransport();
		
		inTransport.close();
		outTransport.close();
	}

	@Override
	public boolean validateObject(TServiceClient client) {
		TTransport inTransport = client.getInputProtocol().getTransport();
		TTransport outTransport = client.getOutputProtocol().getTransport();

		return inTransport.isOpen() && outTransport.isOpen();
	}

	@Override
	public void activateObject(TServiceClient client) throws Exception {
		super.activateObject(client);
	}

	@Override
	public void passivateObject(TServiceClient client) throws Exception {
		super.passivateObject(client);
	}
	
}
