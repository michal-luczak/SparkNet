package io.sparkmc.packet.callback;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class DefaultCallbackRegistry implements CallbackRegistry {

    private final Map<UUID, Consumer<Serializable>> callbacks = new HashMap<>();

    @Override
    public <T extends Serializable> String registerCallback(T packet, Consumer<T> callback) {
        UUID randomUUID = UUID.randomUUID();
        callbacks.put(randomUUID, (Consumer<Serializable>) callback);
        return randomUUID.toString();
    }

    @Override
    public void unregisterCallback(String id) {
        callbacks.remove(UUID.fromString(id));
    }

    @Override
    public Optional<Consumer<Serializable>> findCallback(String packetId) {
        Consumer<Serializable> consumer = callbacks.get(UUID.fromString(packetId));
        if (consumer == null) {
            return Optional.empty();
        } else return Optional.of(consumer);
    }
}
