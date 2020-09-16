package com.ericlam.mc.loginsystem.bungee.managers;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.YamlManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.function.ResultParser;
import com.ericlam.mc.loginsystem.bungee.LoginConfig;
import com.ericlam.mc.loginsystem.bungee.LoginLang;
import com.ericlam.mc.loginsystem.bungee.events.PlayerLoggedEvent;
import com.ericlam.mc.loginsystem.bungee.exceptions.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class LoginManager {

    private final PasswordManager passwordManager;
    private final SessionManager sessionManager;
    private final LoginConfig loginConfig;
    private final LoginLang msg;
    private final IPManager ipManager;
    private final Plugin plugin;
    private final Map<UUID, Integer> failMap = new HashMap<>();
    private final Map<UUID, String> loginSessionMap = new ConcurrentHashMap<>();

    public LoginManager(Plugin plugin, YamlManager configManager) {
        this.plugin = plugin;
        this.loginConfig = configManager.getConfigAs(LoginConfig.class);
        this.msg = configManager.getConfigAs(LoginLang.class);
        this.passwordManager = new PasswordManager(plugin);
        this.sessionManager = new SessionManager(loginConfig);
        this.ipManager = new IPManager();
    }

    public CompletableFuture<String> forceUpdateIP(ProxiedPlayer player) {
        return CompletableFuture.supplyAsync(() -> ipManager.updateIP(player));
    }

    public CompletableFuture<Boolean> isMaxAccount(ProxiedPlayer connection) {
        final String ip = IPManager.getIP(connection);
        return CompletableFuture.supplyAsync(() -> ipManager.checkAccount(ip)).thenApply(i -> i >= loginConfig.maxAcPerIP);
    }

    public void updateIPTask(ProxiedPlayer player) {
        if (loginSessionMap.containsKey(player.getUniqueId())) {
            String ip = loginSessionMap.get(player.getUniqueId());
            String nowIP = IPManager.getIP(player);
            if (nowIP.equals(ip)) {
                return;
            }
            plugin.getLogger().info(player.getDisplayName() + " ip does not match (old: " + ip + ", latest: " + nowIP + ")");
            sessionManager.clearSession(player.getUniqueId());
        }
        this.forceUpdateIP(player).thenAccept(ip -> this.loginSessionMap.put(player.getUniqueId(), ip)).whenComplete((v, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                ProxyServer.getInstance().getLogger().log(Level.SEVERE, ex.getMessage());
                return;
            }
            ProxyServer.getInstance().getLogger().info("IP Task update completed.");
        });
    }

    private void handleFail(ProxiedPlayer player) {
        failMap.computeIfPresent(player.getUniqueId(), (p, v) -> ++v);
        failMap.putIfAbsent(player.getUniqueId(), 1);
        int kick = loginConfig.timesBeforeFail;
        int fail = failMap.getOrDefault(player.getUniqueId(), 0);
        if (fail >= kick) {
            player.disconnect(TextComponent.fromLegacyText(msg.getPure("kick-fail")));
            this.clearFail(player);
        }
    }

    public void clearFail(ProxiedPlayer player) {
        failMap.remove(player.getUniqueId());
    }

    public void passLogin(ProxiedPlayer player) {
        PlayerLoggedEvent event = new PlayerLoggedEvent(player, failMap.getOrDefault(player.getUniqueId(), 0), System.currentTimeMillis());
        ProxyServer.getInstance().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            sessionManager.addSession(player.getUniqueId());
            failMap.remove(player.getUniqueId());
        } else {
            this.handleFail(player);
        }
    }

    public boolean notLoggedIn(ProxiedPlayer player) {
        return sessionManager.isExpired(player.getUniqueId());
    }

    public void login(ProxiedPlayer player, final String password) throws AlreadyLoggedException, AccountNonExistException {
        if (!sessionManager.isExpired(player.getUniqueId())) throw new AlreadyLoggedException();
        if (passwordManager.matchPassword(player.getUniqueId(), password)) {
            this.passLogin(player);
        } else {
            this.handleFail(player);
            throw new WrongPasswordException();
        }
    }

    // admin
    public CompletableFuture<Boolean> editPassword(OfflinePlayer player, final String newPassword) throws SamePasswordException, WrongPasswordException {
        return this.editPassword(player.getUniqueId(), player.getName(), passwordManager.getPasswordHash(player.getUniqueId()).orElse(""), newPassword);
    }

    // user
    public CompletableFuture<Boolean> editPassword(ProxiedPlayer player, final String oldPw, final String newPassword) throws SamePasswordException, WrongPasswordException {
        return this.editPassword(player.getUniqueId(), player.getName(), oldPw, newPassword);
    }

    private CompletableFuture<Boolean> editPassword(UUID uuid, String name, final String oldPw, final String newPassword) throws SamePasswordException, WrongPasswordException {
        var newHashed = PasswordManager.hashing(newPassword);
        var oldHashed = PasswordManager.hashing(oldPw);
        var oldPassword = passwordManager.getPasswordHash(uuid).orElse("");
        if (!oldPassword.equals(oldPw)) throw new WrongPasswordException();
        if (oldHashed.equals(newHashed)) throw new SamePasswordException();
        return CompletableFuture.supplyAsync(() -> passwordManager.editPassword(uuid, name, newPassword));
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

    public void tryKick(UUID player, String path) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(player);
        if (proxiedPlayer != null) {
            proxiedPlayer.disconnect(new MessageBuilder(msg.getPure(path)).build());
        }
    }

    public CompletableFuture<Boolean> register(ProxiedPlayer player, final String password, final String confirmPassword) {
        if (!password.equals(confirmPassword)) throw new ConfirmPasswordFailedException();
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.register(player, password)).ifTrue(() -> this.passLogin(player)).getResult());
    }

    public void loadUserData(UUID uuid, Runnable callback) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            passwordManager.getFromSQL(uuid);
            callback.run();
        });
    }


}
