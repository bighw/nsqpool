package com.hyx.nsqjava.core.commands;

public class Nop implements NSQCommand {

    @Override
    public String getCommandString() {
        return "NOP\n";
    }

    @Override
    public byte[] getCommandBytes() {
        return getCommandString().getBytes();
    }

}
