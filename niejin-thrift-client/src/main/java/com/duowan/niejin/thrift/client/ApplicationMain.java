package com.duowan.niejin.thrift.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年2月20日
 *
**/
@SpringBootApplication(scanBasePackages ="com.duowan.niejin.thrift")
public class ApplicationMain implements EmbeddedServletContainerCustomizer{
	public static void main(String[] args) {
		SpringApplication.run(ApplicationMain.class, args);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		container.setPort(8888);
	}
}
