package com.ericlam.mc.loginsystem.exceptions;

import org.bukkit.entity.Player;

public class AccountNonExistException extends AuthException{

    @Override
    public String getPath() {
        return "error.ac-no-exist";
    }
}
