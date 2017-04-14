package com.duowan.niejin.java.demo.netty.heartbeat;

import com.ctc.wstx.util.WordResolver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月5日
 *
 **/
public class Server {

	public static void main(String[] args) {
		NioEventLoopGroup boss = new NioEventLoopGroup(1);
		NioEventLoopGroup work = new NioEventLoopGroup(4);

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boss, work).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new IdleStateHandler(10, 0, 0));
							p.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, -4, 0));
							p.addLast(new ServerHandler());
						}
					});
			
			ChannelFuture future = bootstrap.bind(12345).sync();
			
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			work.shutdownGracefully();
			boss.shutdownGracefully();
		}
	}
}
