package io.sparkmc.client;

import com.esotericsoftware.kryo.kryo5.Kryo;
import io.sparkmc.netty.decoder.DefaultPacketDecoder;
import io.sparkmc.netty.encoder.DefaultPacketEncoder;
import io.sparkmc.netty.handler.ClientPacketReceiveHandler;
import io.sparkmc.netty.handler.ServerPacketReceiveHandler;
import io.sparkmc.packet.callback.CallbackRequestPacketWrapper;
import io.sparkmc.packet.callback.CallbackResponsePacketWrapper;
import io.sparkmc.packet.callback.DefaultCallbackRegistry;
import io.sparkmc.packet.handler.registry.DefaultPacketHandlerRegistry;
import io.sparkmc.packet.registry.KryoPacketRegistry;
import io.sparkmc.packet.serialization.KryoPacketSerializer;

import java.util.Map;
import java.util.Set;

public class SparkNetClientFactory {

    public static SparkClient.SparkNetClientBuilder create() {
        var kryo = new Kryo();
        kryo.register(CallbackResponsePacketWrapper.class);
        kryo.register(CallbackRequestPacketWrapper.class);
        var packetSerializer = new KryoPacketSerializer(kryo);
        var packetHandlerRegistry = new DefaultPacketHandlerRegistry(Map.of(), Map.of());
        var callbackRegistry = new DefaultCallbackRegistry();
        return new NettyClient.NettyClientBuilder()
                .packetSerializer(new KryoPacketSerializer(kryo))
                .packetHandlerRegistry(packetHandlerRegistry)
                .packetRegistry(new KryoPacketRegistry(kryo, Set.of()))
                .callbackRegistry(callbackRegistry)
                .channelHandlers(Set.of(
                        new DefaultPacketEncoder(packetSerializer),
                        new DefaultPacketDecoder(packetSerializer),
                        new ClientPacketReceiveHandler(packetHandlerRegistry, callbackRegistry)
                )).packetSerializer(packetSerializer);
    }
}