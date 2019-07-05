package com.ericlam.mc.loginsystem.bungee.exceptions;

public class PremiumException extends AuthException {
    @Override
    public String getPath() {
        return "error.premium";
    }
}
