package com.duowan.niejin.java.demo.thread.javautilconcurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月20日
 *
**/
public class ProductionAndConsumer3 {

	public static Lock lock = new ReentrantLock();
	
	public static Condition full = lock.newCondition();
	
	public static Condition empty = lock.newCondition();
	
	static class Production implements Runnable{
		
		private List queue;
		private Integer maxCount;
		
		public Production(List queue,Integer maxCount){
			this.queue = queue;
			this.maxCount = maxCount;
		}
		
		@Override
		public void run() {
			while(true){
				if(lock.tryLock()){
					try {
						if(queue.size() >= maxCount){
							System.out.println("production is wait,queue is full");
							try {
								full.await();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						System.out.println("production ing ....");
						 queue.add(new String("niejin"));
						 empty.signal();
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						lock.unlock();
					}
				}else{
					System.out.println("production 未获取锁资源");
				}
			}
		} 
		
	}
	
	static class Consumer implements Runnable{
		private List queue;
		private Integer minCount;
		
		public Consumer(List queue,Integer minCount) {
			this.queue= queue;
			this.minCount = minCount;
		}
		
		@Override
		public void run() {
			while(true){
				if(lock.tryLock()){
					try{
						if(queue.size() <= minCount){
							System.out.println("consumer the queue is empty .");
							empty.await();
						}
						
						System.out.println("consumer ing ...");
						queue.remove(0);
						full.signal();
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						lock.unlock();
					}
				}else{
					System.out.println("consumer 未获取锁资源");
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		List queue = new ArrayList<String>();
		new Thread(new Production(queue,1)).start();
		new Thread(new Consumer(queue,0)).start();
	}
}
