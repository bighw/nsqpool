package com.hyx.nsqjava.enums;

public enum PlatformEnum {

    All(0),
    Android(1),
    IOS(2),
    WinPhone(3),
    Android_ios(4),
    Android_wp(5),
    Aos_wp(6);

    private final int value;

    private PlatformEnum(final int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
