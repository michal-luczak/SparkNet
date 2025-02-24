package io.sparkmc.netty.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.sparkmc.packet.serialization.PacketSerializer;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@ChannelHandler.Sharable
@RequiredArgsConstructor
public class DefaultPacketEncoder extends MessageToByteEncoder<Serializable> {

    private final PacketSerializer packetSerializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) {
        byte[] bytes = packetSerializer.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
