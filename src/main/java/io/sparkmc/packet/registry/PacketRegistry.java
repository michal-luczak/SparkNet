package io.sparkmc.packet.registry;

import java.io.Serializable;
import java.util.Optional;

public interface PacketRegistry {

    <T extends Serializable> void registerPacket(Class<T> packet);

    <T extends Serializable> Optional<Class<T>> findPacket(Class<T> packet);
}
