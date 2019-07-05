package com.ericlam.mc.loginsystem.bungee.commands.admin;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UpdateIPCommand extends AdminAuthNode {
    UpdateIPCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "updateip", "更新玩家IP", null, "ipupdate");
    }

    @Override
    public CompletableFuture<Boolean> execution(ProxiedPlayer player, List<String> list, OfflinePlayer target) throws AuthException {
        if (!target.isOnline()) {
            MessageBuilder.sendMessage(player, HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer());
            return CompletableFuture.completedFuture(false);
        }
        return loginManager.forceUpdateIP(target.getPlayer()).thenApplyAsync(ip -> {
            MessageBuilder.sendMessage(player, configManager.getMessage("update-ip").replace("<ip>", ip));
            return true;
        });
    }
}
