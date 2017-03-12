package com.duowan.niejin.zookeeper.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages="com.duowan.niejin")
public class ApplicationMain {
	public static void main(String[] args) {
		System.out.println("Hello World!");
		SpringApplication.run(ApplicationMain.class, args);
	}
}
