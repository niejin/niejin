package com.duowan.niejin.java.demo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.duowan.niejin.java.demo.util.Proxies;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Pass oass = new Sub();
        Pass proxy = Proxies.JDK_PROXY.newProxy(Pass.class, (InvocationHandler)(new Handler(oass)));
        proxy.method();
    }
}

interface Pass{
	void method();
}

class Sub implements Pass{

	@Override
	public void method() {
		System.out.println("sub method()");
	}
	
}

class Handler implements InvocationHandler{
	
	private Object target;
	
	public Handler(Object target){
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("proxy before");
		Object result = method.invoke(target, args);
		System.out.println("proxy after");
		return result;
	}
	
}