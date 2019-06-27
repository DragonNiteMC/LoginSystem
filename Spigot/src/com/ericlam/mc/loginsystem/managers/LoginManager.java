package com.ericlam.mc.loginsystem.managers;

import com.ericlam.mc.loginsystem.exceptions.AccountNonExistException;
import com.ericlam.mc.loginsystem.exceptions.AlreadyRegisteredException;
import com.ericlam.mc.loginsystem.exceptions.ConfirmPasswordFailedException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class LoginManager {

    private PasswordManager passwordManager;
    private SessionManager sessionManager;
    private Plugin plugin;

    public LoginManager(Plugin plugin) {
        this.plugin = plugin;
        this.passwordManager = new PasswordManager(plugin);
        this.sessionManager = new SessionManager();
    }

    public boolean login(UUID uuid, final String password) throws AccountNonExistException {
        if (!sessionManager.isExpired(uuid)) return true;
        try {
            PasswordManager.hashing(password);
            String expected = passwordManager.getPasswordHash(uuid);
            if (expected.isBlank()) throw new AccountNonExistException();
            boolean result = PasswordManager.hashing(password).equals(expected);
            if (result) sessionManager.addSession(uuid);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean editPassword(@Nonnull OfflinePlayer player, final String password, final String confirmPassword) throws ConfirmPasswordFailedException, AccountNonExistException {
        if (!password.equals(confirmPassword)) throw new ConfirmPasswordFailedException();
        boolean result = passwordManager.editPassword(player, password);
        if (result) sessionManager.clearSession(player.getUniqueId());
        return result;
    }

    public boolean unregister(@Nonnull OfflinePlayer player) throws AccountNonExistException {
        boolean result = passwordManager.unregister(player.getUniqueId());
        if (result) sessionManager.clearSession(player.getUniqueId());
        return result;
    }

    public boolean register(@Nonnull OfflinePlayer player, final String password, final String confirmPassword) throws ConfirmPasswordFailedException, AlreadyRegisteredException {
        if (!password.equals(confirmPassword)) throw new ConfirmPasswordFailedException();
        boolean result = passwordManager.register(player, password);
        if (result) sessionManager.addSession(player.getUniqueId());
        return result;
    }

    public void loadUserData(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> passwordManager.getFromSQL(uuid));
    }


}
