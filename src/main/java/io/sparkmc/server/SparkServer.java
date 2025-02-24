package io.sparkmc.server;

import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.registry.PacketRegistry;

import java.util.concurrent.CompletableFuture;

public interface SparkServer {

    CompletableFuture<Void> run() throws InterruptedException;

    PacketRegistry getPacketRegistry();

    PacketHandlerRegistry getPacketHandlerRegistry();

    void shutdown();

    interface SparkServerBuilder {

        SparkServerBuilder tcpPort(int tcpPort);

        SparkServerBuilder udpPort(int tcpPort);

        SparkServer build();
    }
}
