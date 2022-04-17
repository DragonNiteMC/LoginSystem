package com.ericlam.mc.loginsystem.bungee.commands;

import com.ericlam.mc.bungee.dnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.dnmc.config.YamlManager;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.exceptions.PremiumException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UnRegisterCommand extends FutureAuthCommandNode {

    public UnRegisterCommand(LoginManager loginManager, YamlManager configManager) {
        super(loginManager, configManager, "unregister", "取消註冊", null, "unreg");
    }

    @Override
    public CompletableFuture<Boolean> executeOperation(ProxiedPlayer player, List<String> list) throws AuthException {
        if (loginManager.notLoggedIn(player)) {
            MessageBuilder.sendMessage(player, msg.get("not-logged-in"));
            return CompletableFuture.completedFuture(false);
        }
        if (player.getPendingConnection().isOnlineMode()) throw new PremiumException();
        return loginManager.unregister(player);
    }
}
