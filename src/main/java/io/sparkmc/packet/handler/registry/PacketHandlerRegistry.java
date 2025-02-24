package io.sparkmc.packet.handler.registry;

import io.sparkmc.packet.callback.CallbackPacketHandler;
import io.sparkmc.packet.handler.PacketHandler;

import java.io.Serializable;

public interface PacketHandlerRegistry {

    <T extends Serializable> void registerHandler(Class<T> packetClass, PacketHandler<T> handler);

    <T extends Serializable> void registerCallbackHandler(Class<T> packetClass, CallbackPacketHandler<T> callbackPacketHandler);

    <T extends Serializable> PacketHandler<T> getHandler(Class<T> packetClass);

    <T extends Serializable> CallbackPacketHandler<T> getCallbackHandler(Class<T> packetClass);

}
