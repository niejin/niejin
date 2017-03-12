package com.duowan.niejin.java.demo.io.nio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月4日
 *
 **/
public class EchoClient implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(EchoClient.class);
	// 空闲计数器，如果空闲超过10次 将检测server是否中断连接
	private static int idleCounter = 0;

	private Selector selector;
	private SocketChannel socketChannel;
	private ByteBuffer temp = ByteBuffer.allocate(1024);

	public EchoClient() throws IOException {
		//
		this.selector = Selector.open();

		// 连接远程的echoServer
		socketChannel = SocketChannel.open();
		boolean isConnected = socketChannel.connect(new InetSocketAddress("localhost", 9090));
		socketChannel.configureBlocking(false);
		SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);

		if (isConnected) {
			this.sendFirstMsg();
		} else {
			// 如果连接还在尝试中，则注册connect事件的监听，connect 成功以后会发出connect事件
			key.interestOps(SelectionKey.OP_CONNECT);
		}
	}

	private void sendFirstMsg() throws IOException {
		String msg = "Hello NIO.";
		socketChannel.write(ByteBuffer.wrap(msg.getBytes(Charset.forName("UTF-8"))));
	}

	public static void main(String[] args) throws IOException {
		EchoClient echoClient = new EchoClient();
		new Thread(echoClient).start();
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				// 阻塞，等待时间发生，或者超1s 超时 。num为发生事件的数量
				int num = this.selector.select(1000);
				
				if(num == 0){
					idleCounter ++;
					if(idleCounter > 10){
						try {
							this.sendFirstMsg();
							
						} catch (Exception e) {
							logger.info("连接断开了 ....");
							this.socketChannel.close();
							this.selector.close();
							return;
						}
					}
					
				}else{
					idleCounter = 0;
				}

				Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					it.remove();
					
					if(key.isValid()){
						if(key.isConnectable()){
							SocketChannel socketChannel = (SocketChannel) key.channel();
							if(socketChannel.isConnectionPending()){
								socketChannel.finishConnect();
							}
							this.sendFirstMsg();
						}else if(key.isReadable()){
							// msg received
							SocketChannel sc = (SocketChannel) key.channel();
							
							this.temp = ByteBuffer.allocate(1024);
							int count = sc.read(temp);
							
							if (count < 0) {
								sc.close();
								continue;
							}
							// 切换buffer到读状态,内部指针归位.
							temp.flip();
							String msg = Charset.forName("UTF-8").decode(temp).toString();
							System.out
									.println("Client received [" + msg + "] from server address:" + sc.getRemoteAddress());

							Thread.sleep(1000);
							// echo back.
							sc.write(ByteBuffer.wrap(msg.getBytes(Charset.forName("UTF-8"))));

							// 清空buffer
							temp.clear();
						
						}
					}else{
						key.cancel();
					}
				}
			}
		} catch (Exception e) {

		} finally {

			/*
			 * if(socketChannel != null){ try { socketChannel.close(); } catch
			 * (IOException e) { e.printStackTrace(); } }
			 * 
			 * if(selector != null){ try { selector.close(); } catch
			 * (IOException e) { e.printStackTrace(); } }
			 */
		}
	}

}
