package io.sparkmc.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.sparkmc.CallbackPacket;
import io.sparkmc.ChatPacket;
import io.sparkmc.netty.decoder.DefaultPacketDecoder;
import io.sparkmc.netty.encoder.DefaultPacketEncoder;
import io.sparkmc.netty.handler.ConnectionHandler;
import io.sparkmc.netty.handler.ServerPacketReceiveHandler;
import io.sparkmc.netty.handler.UDPPacketReceiveHandler;
import io.sparkmc.packet.callback.CallbackRegistry;
import io.sparkmc.packet.callback.CallbackResponsePacketWrapper;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.registry.PacketRegistry;
import io.sparkmc.packet.serialization.PacketSerializer;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Builder
class NettyServer implements SparkServer {

    private final int tcpPort;
    private final int udpPort;
    @Getter
    private final PacketHandlerRegistry packetHandlerRegistry;
    @Getter
    private final PacketRegistry packetRegistry;
    private final PacketSerializer packetSerializer;
    private final CallbackRegistry callbackRegistry;
    private final ChannelGroup activeChannels;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup udpGroup;

    public static void main(String[] args) throws InterruptedException {
        SparkServer server = SparkNetServerFactory.create()
                .tcpPort(8080)
                .udpPort(8081)
                .build();
        server.getPacketRegistry().registerPacket(ChatPacket.class);
        server.getPacketRegistry().registerPacket(CallbackPacket.class);
        server.getPacketHandlerRegistry().registerHandler(ChatPacket.class, (packet, ctx) -> {
            System.out.println(packet.player() + ": " + packet.message() + " -> " + packet.target());
        });
        server.getPacketHandlerRegistry().registerCallbackHandler(CallbackPacket.class, (packet, ctx) -> {
            System.out.println("Received: " + packet.serializable().test());
            ctx.writeAndFlush(new CallbackResponsePacketWrapper(packet.id(), packet.serializable()));
        });
        server.run();
    }

    @Override
    public CompletableFuture<Void> run() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        // Dodajemy hook do zamknięcia przy zakończeniu aplikacji
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        // 📌 TCP Server
        ServerBootstrap tcpBootstrap = new ServerBootstrap();
        tcpBootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(
                                new DefaultPacketEncoder(packetSerializer),
                                new DefaultPacketDecoder(packetSerializer),
                                new ServerPacketReceiveHandler(packetHandlerRegistry, callbackRegistry),
                                new ConnectionHandler(activeChannels)
                        );
                        System.out.println("TCP Channel initialized");
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // 📌 UDP Server
        Bootstrap udpBootstrap = new Bootstrap();
        udpBootstrap.group(udpGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new UDPPacketReceiveHandler(packetHandlerRegistry, packetSerializer));

        // 📌 Uruchomienie TCP Server w asynchroniczny sposób
        CompletableFuture<Void> tcpFuture = new CompletableFuture<>();
        tcpBootstrap.bind(tcpPort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("TCP Server started successfully.");
                tcpFuture.complete(null);
            } else {
                tcpFuture.completeExceptionally(future.cause());
            }
        });

        // 📌 Uruchomienie UDP Server w asynchroniczny sposób
        CompletableFuture<Void> udpFuture = new CompletableFuture<>();
        udpBootstrap.bind(udpPort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("UDP Server started successfully.");
                udpFuture.complete(null);
            } else {
                udpFuture.completeExceptionally(future.cause());
            }
        });

        // 📌 Po zakończeniu uruchamiania obu serwerów, kończymy metodę
        return CompletableFuture.allOf(tcpFuture, udpFuture).thenRun(() -> {
            System.out.println("Both TCP and UDP servers are running.");
            completableFuture.complete(null);
        }).exceptionally(ex -> {
            completableFuture.completeExceptionally(ex);
            return null;
        });
    }

    @SneakyThrows
    @Override
    public void shutdown() {
        activeChannels.close();
        workerGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS).sync();
        bossGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS).sync();
        udpGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS).sync();
    }

    static class NettyServerBuilder implements SparkServerBuilder {

    }
}
