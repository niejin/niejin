package com.duowan.niejin.thirft.support;

import org.apache.thrift.TServiceClient;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月7日
 *
 **/
public interface PoolOperationCallBack {
	// 销毁之前执行
	void destroy(TServiceClient client);

	// 创建成功时执行
	void make(TServiceClient client);
}
