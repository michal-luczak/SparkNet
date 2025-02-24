package io.sparkmc.packet.callback;

import java.io.Serializable;

public record CallbackRequestPacketWrapper<T extends Serializable>(String id, T serializable) implements Serializable {

}
