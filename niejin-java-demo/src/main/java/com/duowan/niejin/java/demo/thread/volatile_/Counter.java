package com.duowan.niejin.java.demo.thread.volatile_;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月6日
 *
**/
public class Counter {
	public static volatile int count = 0;
	
	private static CountDownLatch countDownLatch = new CountDownLatch(1000);
	
	public static void incr(){
		try {
			//延迟1s 使得结果更明显
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		count ++;
		countDownLatch.countDown();
	}
	
	public static void main(String[] args) {
		for(int i = 0 ; i < 1000 ; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					Counter.incr();					
				}
			}).start();
		}
		
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("运行的结果：Counter.count = " + Counter.count);
	}
}
