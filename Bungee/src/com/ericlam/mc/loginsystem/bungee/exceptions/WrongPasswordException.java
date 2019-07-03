package com.ericlam.mc.loginsystem.bungee.exceptions;

public class WrongPasswordException extends AuthException {
    @Override
    public String getPath() {
        return "error.wrong-pw";
    }
}
