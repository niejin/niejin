package com.duowan.niejin.java.demo.commons.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月8日
 *
**/
public class SimpleThread extends Thread {
	private volatile boolean isRunning;
	
	private GenericObjectPool pool;
	
	public synchronized void setIsRunning(boolean flag){
		isRunning = flag;
		if(isRunning){
			this.notify();
		}
	}
	
	public SimpleThread(GenericObjectPool pool){
		this.isRunning = false;
		this.pool = pool;
	}
	
	public void destory(){
		this.interrupt();
	}
	
	public void run(){
		 try {  
	            while (true) {  
	                if (!isRunning) {  
	                    this.wait();  
	                } else {  
	                    System.out.println(this.getName() + "开始处理");  
	                    sleep(5000);  
	                    System.out.println(this.getName() + "结束处理");  
	                    setIsRunning(false);  
	                    try {  
	                        pool.returnObject(this);  
	                    } catch (Exception ex) {  
	                        System.out.println(ex);  
	                    }  
	                }  
	            }  
	        } catch (InterruptedException e) {  
	            System.out.println("我被Interrupted了，所以此线程将被关闭");  
	            return;  
	        }  
	}
}
