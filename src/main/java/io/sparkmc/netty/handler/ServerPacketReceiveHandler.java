package io.sparkmc.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.sparkmc.packet.callback.CallbackPacketHandler;
import io.sparkmc.packet.callback.CallbackRegistry;
import io.sparkmc.packet.callback.CallbackRequestPacketWrapper;
import io.sparkmc.packet.callback.CallbackResponsePacketWrapper;
import io.sparkmc.packet.handler.PacketHandler;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ServerPacketReceiveHandler extends SimpleChannelInboundHandler<Serializable> {

    private final PacketHandlerRegistry packetHandlerRegistry;
    private final CallbackRegistry callbackRegistry;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Serializable packet) {
        if (packet instanceof CallbackResponsePacketWrapper packetWrapper) {
            Optional<Consumer<Serializable>> callback = callbackRegistry.findCallback(packetWrapper.id());
            System.out.println(callback.get());
            callback.ifPresent(consumer -> consumer.accept(packetWrapper.response()));
            callbackRegistry.unregisterCallback(packetWrapper.id());
            return;
        }
        if (packet instanceof CallbackRequestPacketWrapper<? extends Serializable> packetWrapper) {
            CallbackPacketHandler<Serializable> callbackHandler = (CallbackPacketHandler<Serializable>) packetHandlerRegistry.getCallbackHandler(packetWrapper.serializable().getClass());
            if (callbackHandler != null) {
                callbackHandler.handlePacket((CallbackRequestPacketWrapper<Serializable>) packetWrapper, ctx);
            } else {
                System.err.println("Brak handlera dla pakietu: " + packet.getClass().getSimpleName());
            }
            return;
        }
        PacketHandler<Serializable> handler = (PacketHandler<Serializable>) packetHandlerRegistry.getHandler(packet.getClass());
        if (handler != null) {
            handler.handle(packet, ctx);
        } else {
            System.err.println("Brak handlera dla pakietu: " + packet.getClass().getSimpleName());
        }
    }
}