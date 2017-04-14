package com.duowan.niejin.java.demo.thread.javautilconcurrent;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月20日
 *
 **/
public class Loop {
	public static Object lock = new Object();
	
	public static CountDownLatch count = new CountDownLatch(20);
	
	public static void main(String[] args) throws InterruptedException {
		new Thread(new LoopThread("main", 1)).start();
		new Thread(new LoopThread("sub",2)).start();
		
		count.await();
		LoopThread.isRun = false;
		
	}
}

class LoopThread implements Runnable {
	String threadName;
	Integer loopCount;
	public static volatile boolean isRun = true;

	public LoopThread(String threadName,Integer loopCount) {
		this.loopCount = loopCount;
		this.threadName = threadName;
	}

	@Override
	public void run() {
		synchronized (Loop.lock) {
			while (isRun) {
				Loop.count.countDown();
				Loop.lock.notify();
				for (int i = 0; i < loopCount; i++) {
					System.out.println(threadName + ",loop :" + i);
				}
				try {
					Loop.lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

}