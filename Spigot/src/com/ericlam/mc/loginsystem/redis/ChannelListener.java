package com.ericlam.mc.loginsystem.redis;

import com.ericlam.mc.loginsystem.managers.LoginManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class ChannelListener extends JedisPubSub {

    private LoginManager loginManager;

    public ChannelListener(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    @Override
    public void onMessage(String channel, String message) {
        String[] params = message.split("_");
        String method = params[0].toLowerCase();
        UUID uuid = UUID.fromString(params[1]);
        switch (method) {
            case "login-pass":
                loginManager.passLogin(uuid);
                break;
            default:
                break;
        }
    }
}
