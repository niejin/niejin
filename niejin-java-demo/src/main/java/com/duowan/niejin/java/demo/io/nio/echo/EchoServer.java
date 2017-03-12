package com.duowan.niejin.java.demo.io.nio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
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
 *       Client连接server以后,将发送一条消息给server. Server会原封不懂的把消息发送回来.
 *       Client再把消息发送回去.Server再发回来.用不休止.
 * 
 *       Server端启动了2个线程,connectionBell线程用于巡逻新的连接事件. readBell线程用于读取所有channel的数据.
 *
 **/
public class EchoServer {
	private static final Logger logger = LoggerFactory.getLogger(EchoServer.class);

	public static SelectorLoop connectionBell; // 轮询是否有新的连接事件[Thread]
	public static SelectorLoop readBell;// 读取所有channel的数据[Thread]

	public boolean isReadBellRunning = false;

	private void startServer() throws IOException {
		// 准备好一个闹钟.当有链接进来的时候响.
		connectionBell = new SelectorLoop();

		// 准备好一个闹装,当有read事件进来的时候响.
		readBell = new SelectorLoop();

		// 开启一个server channel 来监听
		ServerSocketChannel ssc = ServerSocketChannel.open();
		// 开启非阻塞
		ssc.configureBlocking(false);

		ServerSocket socket = ssc.socket();
		socket.bind(new InetSocketAddress("localhost", 9090));

		// 给闹钟规定好要监听报告的时间，这个闹钟只是监听新连接的事件
		ssc.register(connectionBell.getSelector(), SelectionKey.OP_ACCEPT);

		new Thread(connectionBell).start();
	}

	public static void main(String[] args) throws IOException {
		new EchoServer().startServer();
	}

	// Selector轮询线程类
	public class SelectorLoop implements Runnable {

		private Selector selector;

		private ByteBuffer tempBuffer = ByteBuffer.allocate(1024);

		public SelectorLoop() throws IOException {
			this.selector = Selector.open();
		}

		public Selector getSelector() {
			return this.selector;
		}

		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					// 阻塞，只有至少有一个注册事件发生的时候才会继续。
					// this.selector.select();
					// 设置一个超时时间来避免阻塞
					if (this.selector.select(1000) == 0) {
						//logger.info("==========no channel register ======");
						continue;
					}

					Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						// 这个已经处理的key 一定要移除，如果不移除，就会一直在selector.selectedKeys集合中
						// 待到下一次selector.select() > 0时 这个key又会被处理一次
						it.remove();

						// 处理事件，可以用多线程来处理
						this.dispatch(key);
					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					if (this.selector.isOpen()) {
						this.selector.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void dispatch(SelectionKey key) throws IOException, InterruptedException {
			if (key.isValid()) {
				if (key.isAcceptable()) {
					logger.info(" ======== channel 通道已经准备好了 =========");
					ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
					// 接受socket 连接
					SocketChannel socketChannel = serverSocketChannel.accept();

					try {
						registerSocketChannel(socketChannel, SelectionKey.OP_READ);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				} else if (key.isReadable()) {
					logger.info(" ======== channel 通道可以读取 =========");
					// 这是一个read事件，并且这个事件是注册在SocketChannel上的
					SocketChannel socketChannel = (SocketChannel) key.channel();

					try {
						readSocketChannel(socketChannel);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
						socketChannel.close();
						key.cancel();
					}
				}
			} else {
				// 客户端已经断开
				key.cancel();
				logger.info("连接断开，cancel .... ");
			}
		}

		private void readSocketChannel(SocketChannel socketChannel) throws IOException {
			try {
				int count = socketChannel.read(tempBuffer);
				// 如果缓存区中没有任何数据（但实际上这个不太可能，否则就不会触发OP_READ事件了）
				if (count < 0) {
					//throw new IOException("socketChannel [socket] 连接丢失...");
					logger.warn("===========  channel 没有数据  ========= ");
					socketChannel.close();
					return;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				/*socketChannel.close();
				return;*/
				throw new IOException("客户端已经关闭了！");
				
			}
			// 将缓存区从写状态切换为读状态（实际上这个方法是读写模式互切换）。
			// 这是java nio框架中的这个socket channel的写请求将全部等待。
			tempBuffer.flip();
			String message = Charset.forName("utf-8").decode(tempBuffer).toString();
			logger.info("Server recevied[" + message + "]From client addrsss :" + socketChannel.getRemoteAddress());
			// sleep 1s
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// echo back
			socketChannel.write(ByteBuffer.wrap(message.getBytes(Charset.forName("utf-8"))));

			// 清空buffer
			tempBuffer.clear();

		}

		private void registerSocketChannel(SocketChannel socketChannel, int selectionKey) throws IOException {
			socketChannel.configureBlocking(false);
			// 对新的连接的channel注册read时间，使用readBell闹钟
			socketChannel.register(readBell.getSelector(), selectionKey);

			// 如果读取线程还没有启动，那就启动一个读取线程
			synchronized (EchoServer.class) {
				if (!EchoServer.this.isReadBellRunning) {
					EchoServer.this.isReadBellRunning = true;
					new Thread(readBell).start();
				}
			}
		}

	}
}
