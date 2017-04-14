package com.duowan.niejin.java.demo.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    
 *
**/
public class Main {

	public void method(){
		String s1 = new String();
	}
	
	public void method2() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class clazz = Class.forName("java.lang.String");
		String s1 = (String) clazz.newInstance();
	}
	

	public static void main(String[] args) throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
	}
}
