package com.duowan.niejin.java.demo.netty.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月5日
 *
 **/
public abstract class CustomHeartbeatHandler extends SimpleChannelInboundHandler<ByteBuf> {
	public static final byte PING_MSG = 0x01;
	public static final byte PONG_MSG = 0x02;
	public static final byte CUSTOM_MSG = 0x03;

	protected String name;

	private volatile int heartbeatCount = 0;

	public CustomHeartbeatHandler(String name) {
		this.name = name;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		if (msg.getByte(4) == PING_MSG) {
			sendPongMsg(ctx);
		} else if (msg.getByte(4) == PONG_MSG) {
			System.out.println(name + " get pong msg from " + ctx.channel().remoteAddress());
		} else {
			handleData(ctx, msg);
		}

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.err.println("---" + ctx.channel().remoteAddress() + " is active---");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.err.println("---" + ctx.channel().remoteAddress() + " is inactive---");
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
		if (evt instanceof IdleStateEvent) {
			IdleState stat = ((IdleStateEvent) evt).state();
			switch (stat) {
			case READER_IDLE:
				idleReader(ctx);
				break;
			case WRITER_IDLE:
				idleWriter(ctx);
				break;
			case ALL_IDLE:
				idleAll(ctx);
				break;
			default:
				break;
			}
		}
		super.userEventTriggered(ctx, evt);
	}

	protected void idleAll(ChannelHandlerContext ctx) {
		System.err.println("---ALL_IDLE---");
	}

	protected void idleWriter(ChannelHandlerContext ctx) {
		System.err.println("---WRITER_IDLE---");
	}

	protected void idleReader(ChannelHandlerContext ctx) {
		System.err.println("---READER_IDLE---");
	}

	protected void sendPingMsg(ChannelHandlerContext context) {
		ByteBuf buf = context.alloc().buffer(5);
		buf.writeInt(5);
		buf.writeByte(PING_MSG);
		buf.retain();
		context.writeAndFlush(buf);
		heartbeatCount++;
		System.out.println(
				name + " sent ping msg to " + context.channel().remoteAddress() + ", count: " + heartbeatCount);
	}

	private void sendPongMsg(ChannelHandlerContext context) {
		ByteBuf buf = context.alloc().buffer(5);
		buf.writeInt(5);
		buf.writeByte(PONG_MSG);
		context.channel().writeAndFlush(buf);
		heartbeatCount++;
		System.out.println(
				name + " sent pong msg to " + context.channel().remoteAddress() + ", count: " + heartbeatCount);
	}

	protected abstract void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf);

}
