package com.ericlam.mc.loginsystem.bungee.commands;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class LoginCommand extends AuthCommandNode {
    public LoginCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "login", "登入伺服器", "<password>", "l");
    }

    @Override
    public void executeAuth(ProxiedPlayer player, List<String> list) throws AuthException {
        String password = list.get(0);
        loginManager.login(player, password);
    }
}
