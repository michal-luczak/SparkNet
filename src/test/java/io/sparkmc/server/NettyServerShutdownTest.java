package io.sparkmc.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.sparkmc.packet.callback.CallbackRegistry;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.registry.PacketRegistry;
import io.sparkmc.packet.serialization.PacketSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NettyServerShutdownTest {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup udpGroup;
    private NettyServer server;

    @BeforeEach
    void setUp() {
        bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());;
        udpGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());;

        server = NettyServer.builder()
                .tcpPort(8080)
                .udpPort(8081)
                .packetHandlerRegistry(Mockito.mock(PacketHandlerRegistry.class))
                .packetRegistry(Mockito.mock(PacketRegistry.class))
                .packetSerializer(Mockito.mock(PacketSerializer.class))
                .callbackRegistry(Mockito.mock(CallbackRegistry.class))
                .activeChannels(Mockito.mock(DefaultChannelGroup.class))
                .bossGroup(bossGroup)
                .workerGroup(workerGroup)
                .udpGroup(udpGroup)
                .build();
    }

    @Test
    void testServerShutdown() {
        server.run().thenRun(() -> {
            server.shutdown();
            assertThat(bossGroup.isTerminated()).isTrue();
            assertThat(workerGroup.isTerminated()).isTrue();
            assertThat(udpGroup.isTerminated()).isTrue();
            assertThat(workerGroup.isShutdown()).isTrue();
            assertThat(bossGroup.isShutdown()).isTrue();
            assertThat(udpGroup.isShutdown()).isTrue();
        }).join();
    }
}
