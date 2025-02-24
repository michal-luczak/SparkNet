package io.sparkmc.client;

import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.registry.PacketRegistry;

import java.io.Serializable;
import java.util.function.Consumer;

public interface SparkClient {

    void connect();

    void shutdown();

    <T extends Serializable> void sendPacket(T packet, Protocol protocol);

    <T extends Serializable> void sendPacketWithCallback(T packet, Protocol protocol, Consumer<T> callback);

    PacketRegistry getPacketRegistry();

    PacketHandlerRegistry getPacketHandlerRegistry();

    enum Protocol {
        UDP, TCP
    }

    interface SparkNetClientBuilder {

        SparkNetClientBuilder tcpPort(int port);

        SparkNetClientBuilder udpPort(int port);

        SparkNetClientBuilder host(String host);

        SparkClient build();
    }
}
