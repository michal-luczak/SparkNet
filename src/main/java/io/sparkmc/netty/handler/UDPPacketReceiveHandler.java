package io.sparkmc.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.sparkmc.packet.handler.PacketHandler;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.serialization.PacketSerializer;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
public class UDPPacketReceiveHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final PacketHandlerRegistry packetHandlerRegistry;
    private final PacketSerializer packetSerializer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf buf = packet.content();

        // 📌 Sprawdź czy bufor ma dane
        if (!buf.isReadable()) return;

        // 📌 Tworzymy nową tablicę bajtów o odpowiednim rozmiarze
        byte[] bytes = new byte[buf.readableBytes()];

        // 📌 Kopiujemy dane do tablicy bajtów
        buf.getBytes(buf.readerIndex(), bytes);

        // 📌 Deserializacja pakietu
        Serializable deserialized = (Serializable) packetSerializer.deserialize(bytes);

        // 📌 Obsługa pakietu
        PacketHandler<Serializable> handler = (PacketHandler<Serializable>) packetHandlerRegistry.getHandler(deserialized.getClass());
        if (handler != null) {
            handler.handle(deserialized, ctx);
        } else {
            System.err.println("Brak handlera dla pakietu: " + deserialized.getClass().getSimpleName());
        }
    }

}
