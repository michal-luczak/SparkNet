package io.sparkmc.packet.handler.registry;

import io.sparkmc.packet.callback.CallbackPacketHandler;
import io.sparkmc.packet.handler.PacketHandler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DefaultPacketHandlerRegistry implements PacketHandlerRegistry {

    private final Map<Class<? extends Serializable>, PacketHandler<? extends Serializable>> handlers;
    private final Map<Class<? extends Serializable>, CallbackPacketHandler<?>> callbackHandlers;

    public <T extends Serializable> DefaultPacketHandlerRegistry(Map<Class<T>, PacketHandler<T>> handlers, Map<Class<T>, CallbackPacketHandler<T>> callbackHandlers) {
        this.handlers = new HashMap<>(handlers);
        this.callbackHandlers = new HashMap<>(callbackHandlers);
    }

    @Override
    public <T extends Serializable> void registerHandler(Class<T> packetClass, PacketHandler<T> handler) {
        handlers.put(packetClass, handler);
    }

    @Override
    public <T extends Serializable> void registerCallbackHandler(Class<T> packetClass, CallbackPacketHandler<T> callbackPacketHandler) {
        callbackHandlers.put(packetClass, callbackPacketHandler);
    }

    public <T extends Serializable> PacketHandler<T> getHandler(Class<T> packetClass) {
        return (PacketHandler<T>) handlers.get(packetClass);
    }

    @Override
    public <T extends Serializable> CallbackPacketHandler<T> getCallbackHandler(Class<T> packetClass) {
        return (CallbackPacketHandler<T>) callbackHandlers.get(packetClass);
    }
}
