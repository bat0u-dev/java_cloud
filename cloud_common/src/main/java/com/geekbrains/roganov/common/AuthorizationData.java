package com.geekbrains.roganov.common;

public class AuthorizationData extends AbstractMessage {

    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public AuthorizationData(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
