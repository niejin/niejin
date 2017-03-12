package com.duowan.niejin.java.demo.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月4日
 *
 **/
public class SocketServerDemo {
	static {
		BasicConfigurator.configure();
	}

	private static final Logger logger = LoggerFactory.getLogger(SocketServerDemo.class);

	private static final Integer PORT = 9090;

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(PORT);

		try {
			while (true) {
				// 这里JAVA通过JNI请求操作系统，并一直等待操作系统返回结果（或者出错）
				Socket socket = serverSocket.accept();

				logger.info("accepted");
				
				// 下面我们收取信息（这里还是阻塞式的,一直等待，直到有数据可以接受）
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				Integer sourcePort = socket.getPort();
				int maxLen = 2048;
				byte[] contextBytes = new byte[maxLen];
				int realLen;
				StringBuffer message = new StringBuffer();
				// read的时候，程序也会被阻塞，直到操作系统把网络传来的数据准备好。
				while ((realLen = in.read(contextBytes, 0, maxLen)) != -1) {
					message.append(new String(contextBytes, 0, realLen));
					/*
					 * 我们假设读取到“over”关键字， 表示客户端的所有信息在经过若干次传送后，完成
					 */
					if (message.indexOf("over") != -1) {
						break;
					}
				}
				
				logger.info("服务器收到来自于端口：" + sourcePort + "的信息：" + message);
				//
				out.write("reponse Message!".getBytes());
				
				 //关闭
                out.close();
                in.close();
                socket.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}
	}
}
