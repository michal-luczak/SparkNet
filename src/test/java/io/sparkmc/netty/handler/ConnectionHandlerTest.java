package io.sparkmc.netty.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionHandlerTest {

    private ChannelGroup activeChannels;
    private ConnectionHandler connectionHandler;

    @BeforeEach
    void setUp() {
        activeChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        connectionHandler = new ConnectionHandler(activeChannels);
    }

    @Test
    void testChannelActiveAddsChannel() {
        EmbeddedChannel channel = new EmbeddedChannel(connectionHandler);

        assertTrue(channel.isActive());
        assertTrue(activeChannels.contains(channel));
    }

    @Test
    void testChannelInactiveRemovesChannel() {
        EmbeddedChannel channel = new EmbeddedChannel(connectionHandler);
        channel.close();

        assertFalse(activeChannels.contains(channel));
    }

    @Test
    void testExceptionCaughtClosesChannel() {
        EmbeddedChannel channel = new EmbeddedChannel(connectionHandler);
        Throwable cause = new RuntimeException("Test exception");

        channel.pipeline().fireExceptionCaught(cause);

        assertFalse(channel.isOpen());
    }
}
