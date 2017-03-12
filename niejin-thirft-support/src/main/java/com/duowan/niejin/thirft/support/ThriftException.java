package com.duowan.niejin.thirft.support;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月7日
 *
 **/
public class ThriftException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ThriftException() {
		super();
	}

	public ThriftException(String msg) {
		super(msg);
	}

	public ThriftException(Throwable e) {
		super(e);
	}

	public ThriftException(String msg, Throwable e) {
		super(msg, e);
	}

}
