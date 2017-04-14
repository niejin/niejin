package com.duowan.niejin.java.demo.netty.client;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月30日
 *
 **/
public class NettyClient {

	private static final boolean isSSL = false;// or true

	private static SslContext sslCtx = null;
	static {
		if (isSSL) {
			try {
				sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			} catch (SSLException e) {
				e.printStackTrace();
			}
		}
	}

	private String host;

	private Integer port;

	public NettyClient(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	public void start() {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							if (sslCtx != null) {
								p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
							}
							p.addLast(new ObjectEncoder())
									.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
									.addLast(new ClientHandler());
						}
					});
			ChannelFuture f = b.connect(this.host, this.port).sync();

			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	private static class ClientHandler extends ChannelInboundHandlerAdapter {
		private final String msg = "hello java world";

		@Override
		public void channelActive(ChannelHandlerContext ctx) {
			ctx.writeAndFlush(msg);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			System.out.println(msg);
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) {
			ctx.flush();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			// Close the connection when an exception is raised.
			cause.printStackTrace();
			ctx.close();
		}
	}

	public static void main(String[] args) {
		new NettyClient("127.0.0.1", 9999).start();
	}
}
