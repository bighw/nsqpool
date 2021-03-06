package com.hyx.nsqjava.enums;

import java.util.HashMap;
import java.util.Map;

public enum ResponseType {
    OK("OK"), INVALID("E_INVALID"), BAD_TOPIC("E_BAD_TOPIC"), BAD_BODY("E_BAD_BODY"), BAD_CHANNEL("E_BAD_CHANNEL"), BAD_MESSAGE("E_BAD_MESSAGE"), PUT_FAILED("E_PUT_FAILED"), FINISH_FAILED("E_FIN_FAILED"), REQUEUE_FAILED("E_REQ_FAILED"), CLOSE_WAIT(
            "CLOSE_WAIT"), HEARTBEAT("       _heartbeat_");

    private String code;
    private static Map<String, ResponseType> mappings;

    static {
        mappings = new HashMap<String, ResponseType>();
        for (ResponseType t : ResponseType.values()) {
            mappings.put(t.getCode(), t);
        }
    }

    private ResponseType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ResponseType fromCode(String code) {
        return mappings.get(code);
    }
}
