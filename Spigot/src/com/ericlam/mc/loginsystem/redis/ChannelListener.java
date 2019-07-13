package com.ericlam.mc.loginsystem.redis;

import com.ericlam.mc.loginsystem.main.LoginSystem;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class ChannelListener extends JedisPubSub {

    private LoginSystem loginSystem;

    public ChannelListener(LoginSystem loginSystem) {
        this.loginSystem = loginSystem;
    }

    @Override
    public void onMessage(String channel, String message) {
        String[] params = message.split("_");
        String method = params[0].toLowerCase();
        UUID uuid = UUID.fromString(params[1]);
        switch (method){
            case "gain-perm":
                loginSystem.gainPermission(uuid);
                break;
            default:
                break;
        }
    }
}
