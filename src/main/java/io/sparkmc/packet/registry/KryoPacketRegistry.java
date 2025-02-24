package io.sparkmc.packet.registry;

import com.esotericsoftware.kryo.kryo5.Kryo;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class KryoPacketRegistry implements PacketRegistry {

    @Getter
    private final Kryo kryo;
    private final Set<Class<? extends Serializable>> registeredPackets;

    public KryoPacketRegistry(Kryo kryo, Set<Class<? extends Serializable>> registeredPackets) {
        this.kryo = kryo;
        this.registeredPackets = new HashSet<>(registeredPackets);
    }

    public <T extends Serializable> void registerPacket(Class<T> packetClass) {
        if (registeredPackets.add(packetClass)) {
            kryo.register(packetClass);
        }
    }

    @Override
    public <T extends Serializable> Optional<Class<T>> findPacket(Class<T> packet) {
        return Optional.empty();
    }
}
