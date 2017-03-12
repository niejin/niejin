package com.duowan.niejin.thrift.client;

import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.duowan.niejin.thrift.User;
import com.duowan.niejin.thrift.UserService;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月6日
 *
 **/
public class Client {

	private static final String HOST = "localhost";
	private static final Integer PORT = 9090;
	private static final Integer TIMEOUT = 30000;

	public void startClient(Object... args) {
		TTransport transport = null;
		try {
			transport = new TSocket(HOST, PORT);
			// 協議要和服務端一直
			TProtocol protocol = new TBinaryProtocol(transport);
			/*TProtocol protocol = new TCompactProtocol(transport);
			TProtocol protocol = new TJSONProtocol(transport);*/

			// DemoService.Client client = new DmeoService.Client(protocol);
			transport.open();

			// String result = client.DemeMethod(args);
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			// 處理 服務端 返回值為 null的問題
			if (e instanceof TApplicationException
					&& TApplicationException.MISSING_RESULT == ((TApplicationException) e).getType()) {
				System.out.println("This result of ");
			}
		} finally {
			if (null != transport) {
				transport.close();
			}
		}
	}
	
	
	public void startClientAsync(Object... args) {
		try {
			TAsyncClientManager clientMgr = new TAsyncClientManager();
			TNonblockingTransport transport = new TNonblockingSocket(HOST, PORT, 30000);
			
			TProtocolFactory protocol = new TBinaryProtocol.Factory();
			UserService.AsyncClient asynClient = new UserService.AsyncClient(protocol, clientMgr, transport);
			System.out.println("start async client ...");
			
			
			CountDownLatch latch = new CountDownLatch(1);
			
			asynClient.getUser(12,new AsyncClientCallBack(latch));
			
			boolean wait = latch.await(3000, TimeUnit.MILLISECONDS);
			
			System.out.println("wait : " + wait);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}
	
	public class AsyncClientCallBack implements AsyncMethodCallback<User>{

		private CountDownLatch latch;
		
		public AsyncClientCallBack(CountDownLatch latch){
			this.latch = latch;
		}
		
		@Override
		public void onError(Exception exception) {
			 System.out.println("onError");  
			 latch.countDown();
		}
		
		@Override
		public void onComplete(User response) {
			 System.out.println("onComplete"); 
			 latch.countDown();
		}
		
	}
	
}
