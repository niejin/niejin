package com.duowan.niejin.java.demo.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月4日
 *
 **/
public class SocketClientRequestThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SocketClientRequestThread.class);

	private static final Integer PORT = 9090;

	private Integer threadNum;
	private CountDownLatch countDownLatch;

	public SocketClientRequestThread(Integer threadNum, CountDownLatch countDownLatch) {
		this.threadNum = threadNum;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public void run() {
		Socket socket = null;
		OutputStream clientRequest = null;
		InputStream clientResponse = null;

		try {
			socket = new Socket("localhost", PORT);
			clientRequest = socket.getOutputStream();
			clientResponse = socket.getInputStream();

			// 等待，直到SocketClientDaemon完成所有线程的启动，然后所有线程一起发送请求
			this.countDownLatch.await();
			// 发送请求信息
			clientRequest.write(("这是第" + this.threadNum + " 个客户端的请求。").getBytes());
			clientRequest.write(("over").getBytes());
			clientRequest.flush();

			// 在这里等待，直到服务器返回信息
			SocketClientRequestThread.logger.info("第" + this.threadNum + "个客户端的请求发送完成，等待服务器返回信息");
			int maxLen = 1024;
			byte[] contextBytes = new byte[maxLen];
			int realLen;
			String message = "";
			// 程序执行到这里，会一直等待服务器返回信息（注意，前提是in和out都不能close，如果close了就收不到服务器的反馈了）
			while ((realLen = clientResponse.read(contextBytes, 0, maxLen)) != -1) {
				message += new String(contextBytes, 0, realLen);
			}

			SocketClientRequestThread.logger.info("接收到来自服务器的信息:" + message);
		} catch (Exception e) {

		} finally {
			if (clientRequest != null) {
				try {
					clientRequest.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (clientResponse != null) {
				try {
					clientResponse.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
