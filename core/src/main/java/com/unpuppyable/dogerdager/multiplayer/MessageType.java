package com.unpuppyable.dogerdager.multiplayer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MessageType {
    AUTHORIZE("0"), KEYS_DOWN("1"), KEYS_UP("2"), SHOOT("3"), PING("5"),
    AUTHORIZE_RESPONSE("200"), USER_JOINED("201"), USER_LEFT("202"),
    PONG("205"), GAME_STARTED("210"),
    ENTITIES_INIT("250"), ENTITIES_UPDATE("251"), ERROR("400");

    public final String code;
    MessageType(String code) {this.code = code;}

    private static final Map<String, MessageType> BY_CODE =
            Arrays.stream(values()).collect(Collectors.toMap(m -> m.code, m -> m));

    public static MessageType fromCode(String code) {
        return BY_CODE.get(code);
    }
}
