package io.arusland.bots.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyAuth extends Authenticator {
    private final PasswordAuthentication auth;

    public ProxyAuth(String userName, String password) {
        auth = new PasswordAuthentication(userName, password.toCharArray());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }
}
