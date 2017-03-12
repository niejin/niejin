package com.duowan.niejin.thrift.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年2月20日
 *
**/
@SpringBootApplication
public class ApplicationMain {
	public static void main(String[] args) {
		System.out.println("start");
		new SpringApplication(ApplicationMain.class).run(args);
	}
}
