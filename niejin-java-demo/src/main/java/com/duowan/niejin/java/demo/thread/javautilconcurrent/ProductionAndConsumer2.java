package com.duowan.niejin.java.demo.thread.javautilconcurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月20日
 *
**/
public class ProductionAndConsumer2 {
	static Lock lock = new ReentrantLock();
	
	static class Production implements Runnable{
		@Override
		public void run() {
			while(true){
				
			}
		}
		
	}

	static class Consumer implements Runnable{
		@Override
		public void run() {
			
		}
		
	}
	
	public static void main(String[] args) {
		new Thread(new Production()).start();
		new Thread(new Consumer()).start();
	}
	
}
