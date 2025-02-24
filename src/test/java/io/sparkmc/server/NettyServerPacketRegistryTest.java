package io.sparkmc.server;

import io.netty.channel.group.DefaultChannelGroup;
import io.sparkmc.ChatPacket;
import io.sparkmc.packet.callback.CallbackRegistry;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.registry.PacketRegistry;
import io.sparkmc.packet.serialization.PacketSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NettyServerPacketRegistryTest {

    private NettyServer server;
    private PacketRegistry packetRegistry;

    @BeforeEach
    void setUp() {
        packetRegistry = Mockito.mock(PacketRegistry.class);
        server = NettyServer.builder()
                .tcpPort(8080)
                .udpPort(8081)
                .packetHandlerRegistry(Mockito.mock(PacketHandlerRegistry.class))
                .packetRegistry(packetRegistry)
                .packetSerializer(Mockito.mock(PacketSerializer.class))
                .callbackRegistry(Mockito.mock(CallbackRegistry.class))
                .activeChannels(Mockito.mock(DefaultChannelGroup.class))
                .build();
    }

    @Test
    void testPacketRegistration() {
        server.getPacketRegistry().registerPacket(ChatPacket.class);
        verify(packetRegistry, times(1)).registerPacket(ChatPacket.class);
    }
}
