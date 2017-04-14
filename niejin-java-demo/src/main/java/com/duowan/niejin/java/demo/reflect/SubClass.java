package com.duowan.niejin.java.demo.reflect;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time   
 *
**/
class ParentClass {
	private void method(){
		//System.out.println("parent private method");
	}
	
	public void method1(){
		//System.out.println("parent public method1");
	}
}


public class SubClass extends ParentClass {

	@Override
	public void method1() {
		//System.out.println("subClass override method1");
	}
}
