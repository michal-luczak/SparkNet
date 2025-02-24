package io.sparkmc;

import java.io.Serializable;

public record ChatPacket(String player, String target, String message) implements Serializable {

}
