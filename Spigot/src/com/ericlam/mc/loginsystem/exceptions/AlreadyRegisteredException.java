package com.ericlam.mc.loginsystem.exceptions;

public class AlreadyRegisteredException extends AuthException {
    @Override
    public String getPath() {
        return "error.already-registered";
    }
}
