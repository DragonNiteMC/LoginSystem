package com.ericlam.mc.loginsystem.bungee.exceptions;

public class SamePasswordException extends AuthException {
    @Override
    public String getPath() {
        return "error.same-pw";
    }
}
