package io.sparkmc.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    private final ChannelGroup activeChannels;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        activeChannels.add(ctx.channel());
        System.out.println("📌 Klient połączony: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        activeChannels.remove(ctx.channel());
        System.out.println("❌ Klient rozłączony: " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("⚠️ Błąd na połączeniu: " + cause.getMessage());
        ctx.close();
    }
}
