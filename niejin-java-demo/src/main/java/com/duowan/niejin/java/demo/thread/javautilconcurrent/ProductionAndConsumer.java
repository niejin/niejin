package com.duowan.niejin.java.demo.thread.javautilconcurrent;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月19日
 *
 **/
public class ProductionAndConsumer {
	static Object lock = new Object();

	static class Production implements Runnable {
		@Override
		public void run() {
			while(true){
				synchronized(lock){
					System.out.println("production");
					lock.notify();
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			}

		}

	}

	static class Consumer implements Runnable {
		@Override
		public void run() {
			while(true){
				synchronized (lock) {
					System.out.println("consumer");
					
					lock.notify();
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		new Thread(new Production()).start();
		new Thread(new Consumer()).start();
	}
}
