package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import redis.clients.jedis.Jedis;

import java.util.UUID;

class RedisHandler {

    void permissionGainPublish(UUID uuid) {
        try (Jedis jedis = HyperNiteMC.getAPI().getRedisDataSource().getJedis()) {
            jedis.publish("Login-Slave", "GAIN-PERM_" + uuid.toString());
        }
    }
}
