package io.sparkmc.server;

import io.netty.channel.embedded.EmbeddedChannel;
import io.sparkmc.ChatPacket;
import io.sparkmc.netty.handler.ServerPacketReceiveHandler;
import io.sparkmc.packet.callback.CallbackRegistry;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class ServerPacketReceiveHandlerTest {

    private PacketHandlerRegistry packetHandlerRegistry;
    private CallbackRegistry callbackRegistry;
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        packetHandlerRegistry = Mockito.mock(PacketHandlerRegistry.class);
        callbackRegistry = Mockito.mock(CallbackRegistry.class);
        channel = new EmbeddedChannel(new ServerPacketReceiveHandler(packetHandlerRegistry, callbackRegistry));
    }

    @Test
    void testPacketHandling() {
        ChatPacket packet = new ChatPacket("Player1", "Hello", "Everyone");
        channel.writeInbound(packet);

        //verify(packetHandlerRegistry, times(1)).handlePacket(eq(packet), any());
    }
}
