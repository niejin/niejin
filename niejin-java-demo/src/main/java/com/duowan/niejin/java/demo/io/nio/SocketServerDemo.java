package com.duowan.niejin.java.demo.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.http.entity.mime.content.ByteArrayBody;
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

		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress("localhost", PORT));

		Selector selector = Selector.open();
		// 注意，服务器通道只能注册SelectionKey.OP_ACCEPT事件
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		try {
			while (true) {
				// 如果条件成立，说明本次询问selector，并没有获取到任何准备好的，感兴趣的事件
				// java程序对多路复用IO的支持包括了阻塞模式 和 非阻塞模式两种
				if (selector.select(1000) == 0) {
					// ==================================
					// 这里面根绝业务情况，可以做一些然并卵的事情
					// ==================================
					continue;
				}
				// 这里就是本次询问操作系统，所获取到的“所关心的事件”的事件类型（每一个通道都是独立的）
				Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();

				while (selectionKeys.hasNext()) {
					SelectionKey readyKey = selectionKeys.next();
					// 这个已经处理的readyKey 一定要移除，如果不移除，就会一直在selector.selectedKeys集合中
					// 待到下一次selector.select() > 0时 这个readKey又会被处理一次
					selectionKeys.remove();

					SelectableChannel selectableChannel = readyKey.channel();
					if (readyKey.isValid() && readyKey.isAcceptable()) {
						logger.info("===============channel 通道已经准备好了 ==============");
						/*
						 * 当server socket channel通道已经准备好，就可以从server socket
						 * channel中获取socketchannel了 拿到socket
						 * channel后，要做的事情就是马上到selector注册这个socket channel感兴趣的事情。
						 * 否则无法监听到这个socket channel到达的数据
						 */
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectableChannel;
						SocketChannel socketChannel = serverSocketChannel.accept();
						registerSocketChannel(socketChannel, selector);
					} else if (readyKey.isValid() && readyKey.isConnectable()) {
						logger.info("======== socket channel 建立连接============");
					} else if (readyKey.isValid() && readyKey.isReadable()) {
						logger.info("========socket channel 数据准备完成 ，可以去读取数据 ===========");
						readSocketChannel(readyKey);
					}
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			serverSocket.close();
		}

	}

	/**
	 * 在 server socket channel 接收到/准备好 一个新的Tcp连接后 就会向程序返回一个新的socketChannel
	 * 但是这个新的socket channel并没有在selector "选择器/代理器"中注册 所以程序还没法通过selector通知这个socket
	 * channel的事件。 于是 我们拿到新的socket channel 后
	 * 要做的第一件事进就是到selector"选择器/代理器"中注册这个socket channel感兴趣的时间。
	 * 
	 * @param socketChannel
	 *            新的socket channel
	 * @param selector
	 *            “选择器/代理器”
	 * @throws IOException
	 */
	private static void registerSocketChannel(SocketChannel socketChannel, Selector selector) throws IOException {
		socketChannel.configureBlocking(false);
		// socket通道可以且只可以注册三种事件SelectionKey.OP_READ | SelectionKey.OP_WRITE |
		// SelectionKey.OP_CONNECT
		socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(2048));
	}

	/**
	 * 这个方法用户读取从客户端传来的信息 并且观察从客户端过来的socket channel在经过多次传输后，是否完成传输
	 * 如果传输完成，返回一个true的标记
	 * 
	 * @param readyKey
	 * @throws IOException
	 */
	private static void readSocketChannel(SelectionKey readyKey) throws IOException {
		SocketChannel clientSocketChannel = (SocketChannel) readyKey.channel();
		// 获取客户端使用的端口
		InetSocketAddress sourceSocketAddress = (InetSocketAddress) clientSocketChannel.getRemoteAddress();
		Integer sourceSocketPort = sourceSocketAddress.getPort();

		// 拿到这个socket channel使用的缓存区，准备读取数据
		// 在后文中，将详细讲解缓存区的用法缓存，实际上重要的就是三个元素 capacity position limit
		ByteBuffer contextBytes = (ByteBuffer) readyKey.attachment();

		// 将通道的数据写入到缓存区，注意是写入到缓存区。
		// 由于之前设置了ByteBuffer的大小为2048 byte，所以可以存在写入不完的情况
		// 没关系，我们后面来调整代码。这里我们暂时理解为一次接受可以完成
		int realLen = -1;
		try {
			realLen = clientSocketChannel.read(contextBytes);
		} catch (Exception e) {
			// 这里抛出了异常，一般就是客户端因为某种原因终止了。所以关闭channel就行了
			logger.error(e.getMessage(), e);
			clientSocketChannel.close();
			return;
		}

		// 如果缓存区中没有任何数据（但实际上这个不太可能，否则就不会触发OP_READ事件了）
		if (realLen == -1) {
			logger.warn("=======缓存区中没有数据？=======");
			clientSocketChannel.close();
			return;
		}

		// 将缓存区从写状态切换为读状态（实际上这个方法是读写模式互切换）。
		// 这是java nio框架中的这个socket channel的写请求将全部等待。
		contextBytes.flip();
		// 注意中文乱码的问题，我个人喜好是使用URLDecoder/URLEncoder，进行解编码。
		// 当然java nio框架本身也提供编解码方式，看个人咯
		byte[] messageBytes = contextBytes.array();
		String messageEncode = new String(messageBytes, "UTF-8");
		String message = URLDecoder.decode(messageEncode, "UTF-8");

		// 如果收到了“over”关键字，才会清空buffer，并回发数据；
		// 否则不清空缓存，还要还原buffer的“写状态”
		if (message.indexOf("over") != -1) {
			// 清空已经读取的缓存，并从新切换为写状态(这里要注意clear()和capacity()两个方法的区别)
			contextBytes.clear();
			logger.info("端口:" + sourceSocketPort + "客户端发来的信息 ==== Message :" + message);

			// ======================================================
			// 当然接受完成后，可以在这里正式处理业务了
			// ======================================================

			//回发数据 并关闭channel
			ByteBuffer sendBuffer = ByteBuffer.wrap(URLEncoder.encode("回发处理结果", "UTF-8").getBytes());
			clientSocketChannel.write(sendBuffer);
			clientSocketChannel.close();
		}else{
			logger.info("端口：" + sourceSocketPort + "客户端信息还未接受完，继续接受 === Message  ：" + message);
			//这是，limit和capacity的值一致，position的位置是realLen的位置
            contextBytes.position(realLen);
            contextBytes.limit(contextBytes.capacity());			
		}
	}
}
