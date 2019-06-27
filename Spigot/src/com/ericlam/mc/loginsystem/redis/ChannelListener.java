package com.ericlam.mc.loginsystem.redis;

import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class ChannelListener extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        String[] params = message.split("_");
        String method = params[0].toLowerCase();
        UUID uuid = UUID.fromString(params[1]);
        switch (method) {
            case "login-pass":

                break;
            default:
                break;
        }
    }
}
