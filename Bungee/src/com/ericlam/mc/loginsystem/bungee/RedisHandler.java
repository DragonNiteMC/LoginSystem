package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.loginsystem.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.UUID;

public class RedisHandler {


    public boolean commitPlayer(UUID uuid, boolean premium){
        try(Jedis redis = RedisManager.getInstance().getRedis()){
            redis.hset(uuid.toString(), "premium", premium+"");
            return true;
        }catch (JedisException e){
            e.printStackTrace();
        }
        return false;
    }

    public void passLogin(UUID uuid){
        try(Jedis redis = RedisManager.getInstance().getRedis()){
            redis.publish("Login-Slave", "LOGIN-PASS_" + uuid.toString());
        }
    }
}
