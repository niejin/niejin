package com.duowan.niejin.java.demo.netty.server;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年3月30日
 *
 **/
public class NettyServer {

	private int port;
	
	private static final boolean isSSL = false;//or true
	
	private static SslContext sslCtx ;
	
	static{
		if(isSSL){
			SelfSignedCertificate ssc;
			try {
				ssc = new SelfSignedCertificate();
				//sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
				sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			} catch (CertificateException e) {
				e.printStackTrace();
			} catch (SSLException e) {
				e.printStackTrace();
			}
		}else{
			sslCtx = null;
		}
	}

	public NettyServer(int port) {
		this.port = port;
	}

	public void start() {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boss, worker)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							 //ch.pipeline().addLast(new ServerHandler());
							ChannelPipeline p = ch.pipeline();
							if(sslCtx != null){
								p.addLast(sslCtx.newHandler(ch.alloc()));
							}
							p.addLast(new LoggingHandler(LogLevel.INFO))
								.addLast(new ObjectEncoder())
								.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
								.addLast(new ServerHandler());
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = bootstrap.bind(this.port).sync();
			
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			worker.shutdownGracefully();
			boss.shutdownGracefully();
		}
	}
	
	private static class ServerHandler extends ChannelInboundHandlerAdapter{

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			ctx.write("server write msg :" + msg);
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
		
	}
	
	public static void main(String[] args) {
		new NettyServer(9999).start();
	}
}
