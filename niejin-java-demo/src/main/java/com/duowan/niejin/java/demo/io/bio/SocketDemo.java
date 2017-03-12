package com.duowan.niejin.java.demo.io.bio;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月4日
 *
**/
public class SocketDemo {
	public static void main(String[] args) throws InterruptedException {
		Integer clientNums = 20;
		CountDownLatch countDownLatch = new CountDownLatch(clientNums);
		
		for(int i = 0; i < clientNums; i++,countDownLatch.countDown()){
			SocketClientRequestThread client = new SocketClientRequestThread(i, countDownLatch);
			new Thread(client).start();
		}
		
		
		//这个wait不涉及到具体的实验逻辑，只是为了保证守护线程在启动所有线程后，进入等待状态
		synchronized (SocketDemo.class) {
			SocketDemo.class.wait();
		}
	}
	
}

