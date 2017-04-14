package com.duowan.niejin.java.demo.thread.volatile_;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月19日
 *
**/
public class ThreadPool {
	
	public static Executor executor = Executors.newFixedThreadPool(10);
	
	public static ExecutorService executorService = Executors.newFixedThreadPool(10);
	
	
	public static void main(String[] args) {
		
		for(int i =0 ; i< 100;i++){
			/*executor.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(Thread.currentThread().getName()+ " do !");
				}
			});*/
			
		}
		
	}
	
}
