package com.duowan.niejin.java.demo.netty.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月5日
 *
 **/
public class ClientHandler extends CustomHeartbeatHandler {

	private Client client;

	public ClientHandler(Client client) {
		super("client");
		this.client = client;
	}

	@Override
	protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
		byte[] data = new byte[byteBuf.readableBytes() - 5];
		byteBuf.skipBytes(5);
		byteBuf.readBytes(data);
		String content = new String(data);
		System.out.println(name + " get content: " + content);

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		client.doConnect();
	}

	@Override
	protected void idleAll(ChannelHandlerContext ctx) {
		super.idleAll(ctx);
		sendPingMsg(ctx);
	}
}
