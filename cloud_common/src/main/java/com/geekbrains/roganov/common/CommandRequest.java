package com.geekbrains.roganov.common;

public class CommandRequest extends AbstractMessage {
    String command;

    public String getCommand() {
        return command;
    }

    public CommandRequest(String command) {
        this.command = command;
    }
}
