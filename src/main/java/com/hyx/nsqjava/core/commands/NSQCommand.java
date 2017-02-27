package com.hyx.nsqjava.core.commands;

public interface NSQCommand {
    String getCommandString();

    byte[] getCommandBytes();
}
