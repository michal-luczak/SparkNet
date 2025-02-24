package io.sparkmc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.sparkmc.CallbackPacket;
import io.sparkmc.ChatPacket;
import io.sparkmc.netty.handler.UDPPacketReceiveHandler;
import io.sparkmc.packet.callback.CallbackRegistry;
import io.sparkmc.packet.callback.CallbackRequestPacketWrapper;
import io.sparkmc.packet.callback.CallbackResponsePacketWrapper;
import io.sparkmc.packet.handler.registry.PacketHandlerRegistry;
import io.sparkmc.packet.registry.PacketRegistry;
import io.sparkmc.packet.serialization.PacketSerializer;
import io.sparkmc.server.SparkNetServerFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Builder
class NettyClient implements SparkClient {

    private final String host;
    private final int tcpPort;
    private final int udpPort;
    @Getter
    private final PacketRegistry packetRegistry;
    @Getter
    private final PacketHandlerRegistry packetHandlerRegistry;
    private final Set<ChannelHandler> channelHandlers;
    private final PacketSerializer packetSerializer;
    private final CallbackRegistry callbackRegistry;

    private Channel tcpChannel;
    private Channel udpChannel;
    private EventLoopGroup tcpGroup;
    private EventLoopGroup udpGroup;

    public static void main(String[] args) throws InterruptedException {
        SparkClient client = SparkNetClientFactory.create()
                .host("localhost")
                .tcpPort(8080)
                .udpPort(8081)
                .build();
        client.getPacketRegistry().registerPacket(ChatPacket.class);
        client.getPacketRegistry().registerPacket(CallbackPacket.class);
        client.getPacketHandlerRegistry().registerHandler(ChatPacket.class, (packet, ctx) -> {
            System.out.println(packet.player() + ": " + packet.message() + " -> " + packet.target());
        });
        client.getPacketHandlerRegistry().registerCallbackHandler(CallbackPacket.class, (packet, ctx) -> {
            System.out.println("Received: " + packet.serializable().test());
            ctx.writeAndFlush(new CallbackResponsePacketWrapper(packet.id(), packet));
        });
        var server = SparkNetServerFactory.create()
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
            ctx.writeAndFlush(new CallbackResponsePacketWrapper(packet.id(), packet));
        });
        server.run().thenRun(client::connect).thenRun(() -> {
            client.sendPacket(new ChatPacket("tcp1", "da", "asd"), SparkClient.Protocol.TCP);
            client.sendPacket(new ChatPacket("udp1", "da", "asd"), SparkClient.Protocol.UDP);
            client.sendPacket(new ChatPacket("udp2", "da", "asd"), SparkClient.Protocol.UDP);
            client.sendPacket(new ChatPacket("udp3", "da", "asd"), SparkClient.Protocol.UDP);
            client.sendPacket(new ChatPacket("udp4", "da", "asd"), SparkClient.Protocol.UDP);
            client.sendPacket(new ChatPacket("tcp2", "da", "asd"), SparkClient.Protocol.TCP);
            client.sendPacket(new ChatPacket("tcp3", "da", "asd"), SparkClient.Protocol.TCP);
            client.sendPacket(new ChatPacket("tcp4", "da", "asd"), SparkClient.Protocol.TCP);
            client.sendPacketWithCallback(new CallbackPacket("callback"), SparkClient.Protocol.TCP, packet -> {
                System.out.println("Returned: " + packet.test());
            });
        });
    }

    @Override
    public void connect() {
        tcpGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(tcpGroup)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(channelHandlers.toArray(new ChannelHandler[0]));
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, tcpPort).sync();
            tcpChannel = future.channel();
            System.out.println("Połączono z serwerem: " + host + ":" + tcpPort);
        } catch (Exception e) {
            shutdown();
        }

        udpGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(udpGroup)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NioDatagramChannel.class)
                    .handler(new UDPPacketReceiveHandler(packetHandlerRegistry, packetSerializer));

            ChannelFuture future = bootstrap.connect(host, udpPort).sync();
            udpChannel = future.channel();
            System.out.println("Połączono z serwerem: " + host + ":" + udpPort);
        } catch (Exception e) {
            shutdown();
        }
    }

    @Override
    public <T extends Serializable> void sendPacket(T packet, Protocol protocol) {
        switch (protocol) {
            case TCP: {
                if (tcpChannel.isActive()) {
                    tcpChannel.writeAndFlush(packet);
                } else {
                    System.err.println("Nie można wysłać pakietu TCP - brak połączenia!");
                }
                break;
            } case UDP: {
                if (udpChannel.isActive()) {
                    byte[] serialized = packetSerializer.serialize(packet);
                    DatagramPacket datagramPacket = new DatagramPacket(
                            Unpooled.copiedBuffer(serialized),
                            new InetSocketAddress(host, udpPort)
                    );
                    udpChannel.writeAndFlush(datagramPacket);
                } else {
                    System.err.println("Nie można wysłać pakietu UDP - brak połączenia!");
                }
            }
        }
    }

    @Override
    public <T extends Serializable> void sendPacketWithCallback(T packet, Protocol protocol, Consumer<T> callback) {
        String packetId = callbackRegistry.registerCallback(packet, callback);
        tcpChannel.writeAndFlush(new CallbackRequestPacketWrapper<>(packetId, packet)).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                callbackRegistry.unregisterCallback(packetId);
                System.err.println("❌ Nie udało się wysłać pakietu: " + future.cause().getMessage());
            }
        });

        // Timeout dla callbacka (usunie go po 5 sekundach)
        tcpChannel.eventLoop().schedule(() -> {
            callbackRegistry.findCallback(packetId)
                    .ifPresent(foundCallback ->
                            System.err.println("⚠ Timeout dla requestId: " + packetId));
        }, 5, TimeUnit.SECONDS);
    }

    @SneakyThrows
    @Override
    public void shutdown() {
        tcpGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS).sync();
        udpGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS).sync();
    }

    static class NettyClientBuilder implements SparkNetClientBuilder {

    }
}
