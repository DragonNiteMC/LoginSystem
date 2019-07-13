package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.loginsystem.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.UUID;

class RedisHandler {

    void permissionGainPublish(UUID uuid) {
        try (Jedis jedis = RedisManager.getInstance().getRedis()) {
            jedis.publish("Login-Slave", "GAIN-PERM_" + uuid.toString());
        }
    }
}
