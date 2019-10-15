package com.ericlam.mc.loginsystem.bungee.commands.admin;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.YamlManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.loginsystem.bungee.commands.FutureAuthCommandNode;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AdminAuthNode extends FutureAuthCommandNode {

    private final String notfound;

    public AdminAuthNode(LoginManager loginManager, YamlManager configManager, String command, String description, String placeholder, String... alias) {
        super(loginManager, configManager, command, description, "<player>" + (placeholder == null ? "" : " " + placeholder), alias);
        this.notfound = HyperNiteMC.getAPI().getMainConfig().getNoThisPlayer();
    }

    @Override
    public CompletableFuture<Boolean> executeOperation(ProxiedPlayer player, List<String> list) throws AuthException {
        final String name = list.get(0);
        return HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(name).thenComposeAsync((offopt) -> {
            if (offopt.isEmpty()) {
                MessageBuilder.sendMessage(player, notfound);
                return CompletableFuture.completedFuture(false);
            }
            list.remove(0);
            OfflinePlayer offlinePlayer = offopt.get();
            if (offlinePlayer.isPremium()) {
                MessageBuilder.sendMessage(player, HyperNiteMC.getAPI().getMainConfig().getPrefix() + "§e對方是正版玩家！");
                return CompletableFuture.completedFuture(false);
            }
            return this.execution(player, list, offopt.get());
        });
    }

    public abstract CompletableFuture<Boolean> execution(ProxiedPlayer player, List<String> list, OfflinePlayer target) throws AuthException;
}
