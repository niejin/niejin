package com.duowan.niejin.java.demo.netty.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月5日
 *
 **/
public class ServerHandler extends CustomHeartbeatHandler {
	public ServerHandler() {
		super("server");
	}

	@Override
	protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf buf) {
		byte[] data = new byte[buf.readableBytes() - 5];
		ByteBuf responseBuf = Unpooled.copiedBuffer(buf);
		buf.skipBytes(5);
		buf.readBytes(data);
		String content = new String(data);
		System.out.println(name + " get content: " + content);
		channelHandlerContext.write(responseBuf);
	}

	@Override
	protected void idleReader(ChannelHandlerContext ctx) {
		super.idleReader(ctx);
		System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
		ctx.close();
	}
}
