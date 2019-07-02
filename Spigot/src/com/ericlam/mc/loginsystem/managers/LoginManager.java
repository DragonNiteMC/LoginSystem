package com.ericlam.mc.loginsystem.managers;

import com.ericlam.mc.loginsystem.ResultParser;
import com.ericlam.mc.loginsystem.events.PlayerLoggedEvent;
import com.ericlam.mc.loginsystem.exceptions.*;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LoginManager {

    private PasswordManager passwordManager;
    private SessionManager sessionManager;
    private ConfigManager configManager;
    private Set<UUID> premiums = new HashSet<>();
    private Map<Player, Integer> failMap = new Hashtable<>();
    private Plugin plugin;

    public LoginManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.passwordManager = new PasswordManager(plugin);
        this.sessionManager = new SessionManager(configManager);
    }

    public CompletableFuture<Boolean> isPremium(UUID player) {
        if (premiums.contains(player)) return CompletableFuture.completedFuture(true);
        return CompletableFuture.supplyAsync(()-> passwordManager.isPremium(player)).handleAsync((premium, ex) -> {
            if (ex != null){
                ex.printStackTrace();
                return false;
            }
            if (premium) premiums.add(player);
            return premium;
        });
    }

    public void handleFail(Player player){
        int kick = configManager.getData("tbf", Integer.class).orElse(3);
        int fail = failMap.getOrDefault(player, 0);
        if (fail >= kick){
            player.kickPlayer(configManager.getPureMessage("kick-fail"));
            this.clearFail(player);
        }
    }

    public void clearFail(Player player){
        failMap.remove(player);
    }


    public void passLogin(UUID uuid){
        Bukkit.getScheduler().runTask(plugin,()->{
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (!offlinePlayer.isOnline()) return;
            this.passLogin(offlinePlayer.getPlayer());
        });
    }

    public void passLogin(Player player) {
        this.isPremium(player.getUniqueId()).whenComplete((pre, ex)->{
            if (ex != null){
                ex.printStackTrace();
                return;
            }
            PlayerLoggedEvent event = new PlayerLoggedEvent(player, pre);
            plugin.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                sessionManager.addSession(player.getUniqueId());
                failMap.remove(player);
            }else{
                failMap.computeIfPresent(player,(p,v)->++v);
                this.handleFail(player);
            }
        });
    }

    public boolean notLoggedIn(UUID uuid) {
        return sessionManager.isExpired(uuid);
    }

    public void login(@Nonnull Player player, final String password){
        UUID uuid = player.getUniqueId();
        if (!sessionManager.isExpired(uuid)) throw new AlreadyLoggedException();
        PasswordManager.hashing(password);
        String expected = passwordManager.getPasswordHash(uuid);
        if (expected.isBlank()) throw new AccountNonExistException();
        ResultParser.check(() -> PasswordManager.hashing(password).equals(expected)).ifTrue(() -> this.passLogin(player)).ifFalse(()-> {
            failMap.computeIfPresent(player,(p,v)->++v);
            failMap.putIfAbsent(player, 1);
            this.handleFail(player);
            throw new WrongPasswordException();
        });
    }

    public CompletableFuture<Boolean> editPassword(@Nonnull OfflinePlayer player, final String password, final String newPassword){
        if (password.equals(newPassword)) throw new SamePasswordException();
        String oldHash = passwordManager.getPasswordHash(player.getUniqueId());
        String hash = PasswordManager.hashing(password);
        if (!oldHash.equals(hash)) throw new WrongPasswordException();
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.editPassword(player, password)).ifTrue(() -> sessionManager.clearSession(player.getUniqueId())).getResult());
    }

    public CompletableFuture<Boolean> unregister(@Nonnull OfflinePlayer player) throws AccountNonExistException {
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.unregister(player.getUniqueId())).ifTrue(() -> sessionManager.clearSession(player.getUniqueId())).getResult());
    }

    public CompletableFuture<Boolean> register(@Nonnull OfflinePlayer player, final String password, final String confirmPassword){
        if (!password.equals(confirmPassword)) throw new ConfirmPasswordFailedException();
        return CompletableFuture.supplyAsync(() -> ResultParser.check(() -> passwordManager.register(player, password)).ifTrue(() -> sessionManager.addSession(player.getUniqueId())).getResult());
    }

    public void loadUserData(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> passwordManager.getFromSQL(uuid));
    }


}
