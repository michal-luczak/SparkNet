package io.sparkmc.packet.serialization;

import java.io.Serializable;

public interface PacketSerializer {

    <T extends Serializable> byte[] serialize(T packet);

    Object deserialize(byte[] bytes);
}
