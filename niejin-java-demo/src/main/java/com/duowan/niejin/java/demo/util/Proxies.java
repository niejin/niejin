package com.duowan.niejin.java.demo.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import static com.duowan.niejin.java.demo.util.Preconditions.checkArgument;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月5日
 *
 **/
public enum Proxies {
	JDK_PROXY(new ProxyDelegate() {
		@Override
		public <T> T newProxy(Class<T> interfaceType, Object handler) {
			checkArgument(handler instanceof InvocationHandler, "proxy handler error");

			Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class<?>[] { interfaceType },
					(InvocationHandler) handler);
			return interfaceType.cast(object);
		}
	}), CG_LIB(new ProxyDelegate() {
		@Override
		public <T> T newProxy(Class<T> interfaceType, Object handler) {
			checkArgument(handler instanceof InvocationHandler, "proxy handler error");

			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(interfaceType);
			enhancer.setCallback((MethodInterceptor) handler);
			enhancer.setClassLoader(interfaceType.getClassLoader());

			return interfaceType.cast(enhancer.create());
		}
	}), BYTE_BUDDY(new ProxyDelegate() {
		@Override
		public <T> T newProxy(Class<T> interfaceType, Object handler) {
			/*
			 * Class<? extends T> cls = new ByteBuddy() .subclass(interfaceType)
			 * .method(ElementMatchers.isDeclaredBy(interfaceType))
			 * .intercept(MethodDelegation.to(handler, "handler")) .make()
			 * .load(interfaceType.getClassLoader(),
			 * ClassLoadingStrategy.Default.INJECTION) .getLoaded();
			 * 
			 * return Reflects.newInstance(cls);
			 */
			// TODO ...
			return null;
		}
	});

	private ProxyDelegate delegate;

	Proxies(ProxyDelegate delegate) {
		this.delegate = delegate;
	}

	public <T> T newProxy(Class<T> interfaceType,Object handler){
		return this.delegate.newProxy(interfaceType, handler);
	}
	
	public static Proxies getDefalut() {
		return JDK_PROXY;
	}

	interface ProxyDelegate {
		<T> T newProxy(Class<T> interfaceType, Object handler);
	}
}
