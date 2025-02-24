package io.sparkmc;

import io.sparkmc.client.SparkClient;
import io.sparkmc.client.SparkNetClientFactory;
import io.sparkmc.server.SparkNetServerFactory;
import io.sparkmc.server.SparkServer;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(time = 10, timeUnit = TimeUnit.MILLISECONDS)
public class SampleBenchmark {

    private SparkClient client;
    private SparkServer server;

    @Setup(Level.Trial)
    public void setup() throws InterruptedException {
        client = SparkNetClientFactory.create()
                .host("localhost")
                .tcpPort(8080)
                .udpPort(8081)
                .build();
        client.getPacketRegistry().registerPacket(ChatPacket.class);
        client.getPacketRegistry().registerPacket(CallbackPacket.class);
        server = SparkNetServerFactory.create()
                .tcpPort(8080)
                .udpPort(8081)
                .build();
        server.getPacketRegistry().registerPacket(ChatPacket.class);
        server.getPacketHandlerRegistry().registerHandler(ChatPacket.class, (packet1, ctx) -> {});
        server.run().thenRun(client::connect).thenRun(() -> {
            client.sendPacket(new ChatPacket("tcp1", "da", "asd"), SparkClient.Protocol.TCP);
        });
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.println("Zamykam klienta...");
        client.shutdown();
        System.out.println("Zamykam serwer...");
        server.shutdown();
        System.out.println("Zakończono shutdown.");
    }

    @Benchmark
    public void sendPacketTCP() throws InterruptedException {
        client.sendPacket(new ChatPacket("sender", "h", "target"), SparkClient.Protocol.TCP);
    }

    @Benchmark
    public void sendPacketUDP() throws InterruptedException {
        client.sendPacket(new ChatPacket("sender", "h", "target"), SparkClient.Protocol.UDP);
    }
}