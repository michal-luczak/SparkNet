package io.sparkmc.packet.callback;

import java.io.Serializable;

public record CallbackResponsePacketWrapper(String id, Serializable response) implements Serializable {

}
