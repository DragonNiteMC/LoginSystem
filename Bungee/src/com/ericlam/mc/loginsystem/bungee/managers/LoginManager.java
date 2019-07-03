package com.ericlam.mc.loginsystem.bungee.managers;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.loginsystem.ResultParser;
import com.ericlam.mc.loginsystem.bungee.events.PlayerLoggedEvent;
import com.ericlam.mc.loginsystem.bungee.exceptions.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LoginManager {

    private PasswordManager passwordManager;
    private SessionManager sessionManager;
    private ConfigManager configManager;
    private Set<UUID> premiums = new HashSet<>();
    private Map<UUID, Integer> failMap = new Hashtable<>();
    private Plugin plugin;

    public LoginManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.passwordManager = new PasswordManager(plugin);
        this.sessionManager = new SessionManager(configManager);
    }

    public CompletableFuture<Boolean> isPremium(UUID player) {
        if (premiums.contains(player)) return CompletableFuture.completedFuture(true);
        return HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(player).handleAsync((offlinePlayer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return false;
            }
            return offlinePlayer.map(OfflinePlayer::isPremium).orElse(false);
        });
    }

    public void handleFail(ProxiedPlayer player) {
        int kick = configManager.getData("tbf", Integer.class).orElse(3);
        int fail = failMap.getOrDefault(player.getUniqueId(), 0);
        if (fail >= kick){
            player.disconnect(TextComponent.fromLegacyText(configManager.getPureMessage("kick-fail")));
            this.clearFail(player);
        }
    }

    public void clearFail(ProxiedPlayer player) {
        failMap.remove(player.getUniqueId());
    }

    public void passLogin(ProxiedPlayer player) {
        HyperNiteMC.getAPI().getPlayerManager().getOfflinePlayer(player.getUniqueId()).whenComplete((offlinePlayer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            offlinePlayer.ifPresent(this::passLogin);
        });
    }

    public void passLogin(OfflinePlayer player) {
        if (!player.isOnline()) return;
        PlayerLoggedEvent event = new PlayerLoggedEvent(player.getPlayer(), player.isPremium());
        ProxyServer.getInstance().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            sessionManager.addSession(player.getUniqueId());
            failMap.remove(player.getUniqueId());
        } else {
            failMap.computeIfPresent(player.getUniqueId(), (p, v) -> ++v);
            this.handleFail(player.getPlayer());
        }
    }

    public boolean notLoggedIn(UUID uuid) {
        return sessionManager.isExpired(uuid);
    }

    public void login(ProxiedPlayer player, final String password) {
        UUID uuid = player.getUniqueId();
        if (!sessionManager.isExpired(uuid)) throw new AlreadyLoggedException();
        PasswordManager.hashing(password);
        String expected = passwordManager.getPasswordHash(uuid);
        if (expected.isBlank()) throw new AccountNonExistException();
        ResultParser.check(() -> PasswordManager.hashing(password).equals(expected)).ifTrue(() -> this.passLogin(player)).ifFalse(()-> {
            failMap.computeIfPresent(player.getUniqueId(), (p, v) -> ++v);
            failMap.putIfAbsent(player.getUniqueId(), 1);
            this.handleFail(player);
            throw new WrongPasswordException();
        });
    }

    public CompletableFuture<Boolean> editPassword(OfflinePlayer player, final String password, final String newPassword) {
        return this.editPassword(player.getUniqueId(), player.getName(), password, newPassword);
    }

    public CompletableFuture<Boolean> editPassword(ProxiedPlayer player, final String password, final String newPassword) {
        return this.editPassword(player.getUniqueId(), player.getName(), password, newPassword);
    }

    private CompletableFuture<Boolean> editPassword(UUID uuid, String name, final String password, final String newPassword) {
        if (password.equals(newPassword)) throw new SamePasswordException();
        String oldHash = passwordManager.getPasswordHash(uuid);
        String hash = PasswordManager.hashing(password);
        if (!oldHash.equals(hash)) throw new WrongPasswordException();
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.editPassword(uuid, name, password)).ifTrue(() -> sessionManager.clearSession(uuid)).getResult());
    }

    public CompletableFuture<Boolean> unregister(OfflinePlayer player) {
        return this.unregister(player.getUniqueId());
    }

    public CompletableFuture<Boolean> unregister(ProxiedPlayer player) {
        return this.unregister(player.getUniqueId());
    }

    private CompletableFuture<Boolean> unregister(UUID player) {
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.unregister(player)).ifTrue(() -> sessionManager.clearSession(player)).getResult());
    }

    public CompletableFuture<Boolean> register(ProxiedPlayer player, final String password, final String confirmPassword) {
        if (!password.equals(confirmPassword)) throw new ConfirmPasswordFailedException();
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.register(player, password)).ifTrue(() -> sessionManager.addSession(player.getUniqueId())).getResult());
    }

    public void loadUserData(UUID uuid) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> passwordManager.getFromSQL(uuid));
    }


}
