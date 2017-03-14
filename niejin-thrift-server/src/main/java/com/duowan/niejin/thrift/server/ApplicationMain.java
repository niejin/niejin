package com.duowan.niejin.thrift.server;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年2月20日
 *
**/
@SpringBootApplication
public class ApplicationMain {
	public static void main(String[] args) {
		System.out.println("thrift server start .... ");
		SpringApplication app = new SpringApplication(ApplicationMain.class);
		app.setBannerMode(Mode.OFF);
		app.run(args);
	}
}
