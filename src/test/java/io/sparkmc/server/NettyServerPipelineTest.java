package io.sparkmc.server;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.sparkmc.netty.decoder.DefaultPacketDecoder;
import io.sparkmc.netty.encoder.DefaultPacketEncoder;
import io.sparkmc.netty.handler.ConnectionHandler;
import io.sparkmc.netty.handler.ServerPacketReceiveHandler;
import io.sparkmc.packet.callback.CallbackRegistry;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.registry.PacketRegistry;
import io.sparkmc.packet.serialization.PacketSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NettyServerPipelineTest {

    private PacketHandlerRegistry packetHandlerRegistry;
    private PacketRegistry packetRegistry;
    private PacketSerializer packetSerializer;
    private CallbackRegistry callbackRegistry;
    private DefaultChannelGroup activeChannels;

    @BeforeEach
    void setUp() {
        packetHandlerRegistry = Mockito.mock(PacketHandlerRegistry.class);
        packetRegistry = Mockito.mock(PacketRegistry.class);
        packetSerializer = Mockito.mock(PacketSerializer.class);
        callbackRegistry = Mockito.mock(CallbackRegistry.class);
        activeChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @Test
    void testPipelineIsConfiguredCorrectly() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new DefaultPacketEncoder(packetSerializer),
                new DefaultPacketDecoder(packetSerializer),
                new ServerPacketReceiveHandler(packetHandlerRegistry, callbackRegistry),
                new ConnectionHandler(activeChannels)
        );

        assertNotNull(channel.pipeline().get(DefaultPacketEncoder.class));
        assertNotNull(channel.pipeline().get(DefaultPacketDecoder.class));
        assertNotNull(channel.pipeline().get(ServerPacketReceiveHandler.class));
        assertNotNull(channel.pipeline().get(ConnectionHandler.class));
    }
}
