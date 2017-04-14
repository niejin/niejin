package com.duowan.niejin.java.demo.thread.javautilconcurrent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月19日
 *
**/
public class CallableAndFuture {
	
	public static void callableMethod() throws InterruptedException, ExecutionException{
		Callable<Integer> callable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Thread.sleep(10*1000);
				return new Random().nextInt(10);
			}
		};
		
		FutureTask<Integer> future = new FutureTask<Integer>(callable);
		
		new Thread(future).start();
		System.out.println("start");
		Thread.sleep(3*1000); //可以做一点其他的业务上面的事情 等待 future返回
		System.out.println("futureGet:" + future.get());
		//System.in.read();
	}
	
	public static void executorMethod() throws InterruptedException, ExecutionException{
		ExecutorService threadPool = Executors.newSingleThreadExecutor();
		Future<Integer> future = threadPool.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Thread.sleep(10*1000);
				return new Random().nextInt(10);
			}
		});
		System.out.println("start");
		Thread.sleep(3*1000);//TODO Others
		System.out.println("futureGet:" + future.get());
	}

	public static void multiExecutorMethod() throws InterruptedException, ExecutionException{
		ExecutorService threadPool = Executors.newCachedThreadPool();
		CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(threadPool);
		
		for(int i = 1;i<5;i++){
			final int taskID = i;
			cs.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return taskID;
				}
			});
		}
		//TODO Others
		for(int i = 1;i<5;i++){
			System.out.println(cs.take().get());
		}
		
	}
	
	public static void multiExecutorMethod2() throws InterruptedException, ExecutionException{
		ExecutorService threadPool = Executors.newCachedThreadPool();
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		for(int i = 0;i<5;i++){
			final Integer index = i;
			Future<Integer> future = threadPool.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return index;
				}
			});
			futures.add(future);
		}
		
		for(Future<Integer> future : futures){
			System.out.println(future.get());
		}
	}
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		//callableMethod();
		//executorMethod();
		//multiExecutorMethod();
		multiExecutorMethod2();
	}
}
