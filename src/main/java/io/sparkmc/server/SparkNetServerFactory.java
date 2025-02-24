package io.sparkmc.server;

import com.esotericsoftware.kryo.kryo5.Kryo;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.sparkmc.packet.callback.CallbackRequestPacketWrapper;
import io.sparkmc.packet.callback.CallbackResponsePacketWrapper;
import io.sparkmc.packet.callback.DefaultCallbackRegistry;
import io.sparkmc.packet.handler.registry.DefaultPacketHandlerRegistry;
import io.sparkmc.packet.registry.KryoPacketRegistry;
import io.sparkmc.packet.serialization.KryoPacketSerializer;

import java.util.Map;
import java.util.Set;

public class SparkNetServerFactory {

    public static SparkServer.SparkServerBuilder create() {
        var kryo = new Kryo();
        kryo.register(CallbackResponsePacketWrapper.class);
        kryo.register(CallbackRequestPacketWrapper.class);
        var serializer = new KryoPacketSerializer(kryo);
        var packetHandlerRegistry = new DefaultPacketHandlerRegistry(Map.of(), Map.of());
        return NettyServer.builder()
                .packetHandlerRegistry(packetHandlerRegistry)
                .packetRegistry(new KryoPacketRegistry(kryo, Set.of()))
                .callbackRegistry(new DefaultCallbackRegistry())
                .activeChannels(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE))
                .packetSerializer(serializer)
                .bossGroup(new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory()))
                .workerGroup(new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory()))
                .udpGroup(new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory()));
    }
}
