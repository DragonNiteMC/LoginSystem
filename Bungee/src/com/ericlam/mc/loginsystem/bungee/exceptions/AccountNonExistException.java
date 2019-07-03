package com.ericlam.mc.loginsystem.bungee.exceptions;

public class AccountNonExistException extends AuthException{

    @Override
    public String getPath() {
        return "error.ac-no-exist";
    }
}
