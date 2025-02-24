package io.sparkmc.packet.callback;


import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

public interface CallbackRegistry {

    <T extends Serializable> String registerCallback(T packet, Consumer<T> callback);

    void unregisterCallback(String packetId);

    Optional<Consumer<Serializable>> findCallback(String packetId);
}
