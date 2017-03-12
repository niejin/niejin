package com.duowan.niejin.java.demo.commons.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月8日
 *
**/
public class SimpleThreadPoolFactory extends BasePooledObjectFactory<SimpleThread> {

	@Override
	public SimpleThread create() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PooledObject<SimpleThread> wrap(SimpleThread obj) {
		// TODO Auto-generated method stub
		return null;
	}

}
