package com.ericlam.mc.loginsystem.bungee.exceptions;

public class ConfirmPasswordFailedException extends AuthException{
    @Override
    public String getPath() {
        return "error.pw-failed";
    }
}
