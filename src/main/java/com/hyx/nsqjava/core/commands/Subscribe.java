package com.hyx.nsqjava.core.commands;

import com.hyx.nsqjava.enums.CommandType;

public class Subscribe implements NSQCommand {
    private String topic;
    private String channel;
    private String shortId;
    private String longId;

    public Subscribe(String topic, String channel, String shortId, String longId) {
        this.topic = topic;
        this.channel = channel;
        this.shortId = shortId;
        this.longId = longId;

    }

    @Override
    public String getCommandString() {
        return String.format("%s %s %s %s %s\n", CommandType.SUBSCRIBE.getCode(), topic, channel, shortId, longId);
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
