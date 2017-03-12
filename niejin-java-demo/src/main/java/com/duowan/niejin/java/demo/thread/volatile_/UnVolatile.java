package com.duowan.niejin.java.demo.thread.volatile_;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月6日
 *
**/
public class UnVolatile {
	private static  boolean ready;
	private static  int number;
	
	private static class ReaderThread implements Runnable{
		@Override
		public void run() {
			System.out.println("ready:" + ready);
			while(!ready){
				System.out.println("yield");
				Thread.yield();
			}
			System.out.println("number:" + number);
		
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
	/*	new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("ready:" + ready);
				while(!ready){
					System.out.println("yield");
					Thread.yield();
				}
				System.out.println("number:" + number);
			}
		}).start();*/
		
		new Thread(new ReaderThread()).start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		number = 42;
		ready = true;
		
		synchronized(UnVolatile.class){
			UnVolatile.class.wait();
		}
	}
}
