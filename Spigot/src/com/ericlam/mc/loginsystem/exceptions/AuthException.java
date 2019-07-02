package com.ericlam.mc.loginsystem.exceptions;

import org.bukkit.entity.Player;

import java.io.PrintStream;

public abstract class AuthException extends RuntimeException {

   public abstract String getPath();

}
