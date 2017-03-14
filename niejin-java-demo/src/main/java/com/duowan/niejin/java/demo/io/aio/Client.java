package com.duowan.niejin.java.demo.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月12日
 *
**/
public class Client {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		final AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
		Future<Void> future = client.connect(new InetSocketAddress("127.0.0.1", 9090));
		future.get();
		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		client.read(buffer, null, new CompletionHandler<Integer, Void>() {

			@Override
			public void completed(Integer result, Void attachment) {
				System.out.println("client received: " + new String(buffer.array()));
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				exc.printStackTrace();
				try{
					client.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
		});
		
	}
}
