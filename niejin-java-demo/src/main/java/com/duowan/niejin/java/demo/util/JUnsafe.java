package com.duowan.niejin.java.demo.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.misc.Unsafe;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年4月5日
 *
**/
@SuppressWarnings("all")
public class JUnsafe {
	
	private static final Unsafe UNSAFE;
	
	static{
		Unsafe unsafe;
		try{
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			unsafe = (Unsafe) unsafeField.get(null);
		}catch(Throwable  e){
			unsafe = null;
		}
		UNSAFE = unsafe;
	}
	
	public static Unsafe getUnsafe(){
		return UNSAFE;
	}
	
	public static ClassLoader getSystemClassLoader(){
		if(System.getSecurityManager() == null){
			return ClassLoader.getSystemClassLoader();
		}else{
			return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
				@Override
				public ClassLoader run() {
					return ClassLoader.getSystemClassLoader();
				}
			});
		}
	}
}