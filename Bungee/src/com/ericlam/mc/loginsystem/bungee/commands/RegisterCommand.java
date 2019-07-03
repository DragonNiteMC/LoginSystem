package com.ericlam.mc.loginsystem.bungee.commands;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RegisterCommand extends FutureAuthCommandNode{

    public RegisterCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "register", "註冊賬戶", "<password> <confirm password>", "reg");
    }

    @Override
    public CompletableFuture<Boolean> executeOperation(ProxiedPlayer player, List<String> list) throws AuthException {
        final String pw = list.get(0);
        final String confirm = list.get(1);
        return loginManager.register(player, pw, confirm);
    }
}
