package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.loginsystem.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.UUID;

class RedisHandler {


    void notLoginPubish(UUID uuid) {
        try(Jedis redis = RedisManager.getInstance().getRedis()){
            redis.publish("Login-Slave", "NOT-LOGIN_" + uuid.toString());
        }
    }

    void lognPublish(UUID uuid) {
        try(Jedis redis = RedisManager.getInstance().getRedis()){
            redis.publish("Login-Slave", "LOGIN-PASS_" + uuid.toString());
        }
    }

    void permissionGainPublish(UUID uuid) {
        try (Jedis jedis = RedisManager.getInstance().getRedis()) {
            jedis.publish("Login-Slave", "GAIN-PERM_" + uuid.toString());
        }
    }
}
