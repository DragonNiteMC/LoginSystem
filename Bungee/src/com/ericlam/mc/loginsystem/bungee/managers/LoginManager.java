package com.ericlam.mc.loginsystem.bungee.managers;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.YamlManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.function.ResultParser;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
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
    private Map<UUID, Integer> failMap = new HashMap<>();
    private Map<UUID, String> loginSessionMap = new ConcurrentHashMap<>();

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

    public void handleFail(ProxiedPlayer player) {
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
            if (player.isPremium()) {
                sessionManager.addPremiumSession(player.getUniqueId());
            } else {
                sessionManager.addSession(player.getUniqueId());
            }
            failMap.remove(player.getUniqueId());
        } else {
            failMap.computeIfPresent(player.getUniqueId(), (p, v) -> ++v);
            this.handleFail(player.getPlayer());
        }
    }

    public boolean notLoggedIn(ProxiedPlayer player) {
        return sessionManager.isExpired(player.getUniqueId());
    }

    public void login(ProxiedPlayer player, final String password) {
        UUID uuid = player.getUniqueId();
        if (!sessionManager.isExpired(player.getUniqueId())) throw new AlreadyLoggedException();
        String expected = passwordManager.getPasswordHash(uuid);
        if (expected.isBlank()) throw new AccountNonExistException();
        ResultParser.check(() -> PasswordManager.hashing(password).equals(expected)).ifTrue(() -> this.passLogin(player)).ifFalse(() -> {
            failMap.computeIfPresent(player.getUniqueId(), (p, v) -> ++v);
            failMap.putIfAbsent(player.getUniqueId(), 1);
            this.handleFail(player);
            throw new WrongPasswordException();
        });
    }

    public CompletableFuture<Boolean> editPassword(OfflinePlayer player, final String newPassword) {
        return this.editPassword(player.getUniqueId(), player.getName(), passwordManager.getPasswordHash(player.getUniqueId()), newPassword);
    }

    public CompletableFuture<Boolean> editPassword(ProxiedPlayer player, final String password, final String newPassword) {
        return this.editPassword(player.getUniqueId(), player.getName(), PasswordManager.hashing(password), newPassword);
    }

    private CompletableFuture<Boolean> editPassword(UUID uuid, String name, final String passwordHashed, final String newPassword) {
        final String newPasswordHashed = PasswordManager.hashing(newPassword);
        if (passwordHashed.equals(newPasswordHashed)) throw new SamePasswordException();
        final String oldHash = passwordManager.getPasswordHash(uuid);
        if (!oldHash.equals(passwordHashed)) throw new WrongPasswordException();
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.editPassword(uuid, name, newPassword)).ifTrue(() -> sessionManager.clearSession(uuid)).getResult());
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

    public void loadUserData(UUID uuid) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> passwordManager.getFromSQL(uuid));
    }


}
