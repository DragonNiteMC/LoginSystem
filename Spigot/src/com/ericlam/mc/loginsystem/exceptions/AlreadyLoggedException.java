package com.ericlam.mc.loginsystem.exceptions;

public class AlreadyLoggedException extends AuthException{
    @Override
    public String getPath() {
        return "error.already-logged";
    }
}
