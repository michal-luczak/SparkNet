package io.sparkmc.packet.callback;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public interface CallbackPacketHandler<T extends Serializable> {

    void handlePacket(CallbackRequestPacketWrapper<T> packet, ChannelHandlerContext ctx);
}
