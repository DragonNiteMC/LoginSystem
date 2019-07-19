package com.ericlam.mc.loginsystem.bungee.exceptions;

public class MaxAccountReachedException extends AuthException {
    @Override
    public String getPath() {
        return "max-ac";
    }
}
