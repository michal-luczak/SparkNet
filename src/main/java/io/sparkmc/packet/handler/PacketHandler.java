package io.sparkmc.packet.handler;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

@FunctionalInterface
public interface PacketHandler<T extends Serializable> {

    void handle(T packet, ChannelHandlerContext ctx);
}