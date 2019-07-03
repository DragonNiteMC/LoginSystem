package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.loginsystem.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class RedisHandler {


    public void notLoginPubish(UUID uuid) {
        try(Jedis redis = RedisManager.getInstance().getRedis()){
            redis.publish("Login-Slave", "NOT-LOGIN_" + uuid.toString());
        }
    }

    public void lognPublish(UUID uuid) {
        try(Jedis redis = RedisManager.getInstance().getRedis()){
            redis.publish("Login-Slave", "LOGIN-PASS_" + uuid.toString());
        }
    }
}
