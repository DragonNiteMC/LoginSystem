package com.ericlam.mc.loginsystem.bungee.commands;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.exceptions.PremiumException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditPasswordCommand extends FutureAuthCommandNode {
    public EditPasswordCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "editpassword", "修改密碼", "<old-password> <new-password>", "editpw");
    }

    @Override
    public CompletableFuture<Boolean> executeOperation(ProxiedPlayer player, List<String> list) throws AuthException {
        if (loginManager.notLoggedIn(player)) {
            MessageBuilder.sendMessage(player, configManager.getMessage("not-logged-in"));
            return CompletableFuture.completedFuture(false);
        }
        final String oldPw = list.get(0);
        final String newPw = list.get(1);
        if (player.getPendingConnection().isOnlineMode()) throw new PremiumException();
        return loginManager.editPassword(player, oldPw, newPw);
    }
}
