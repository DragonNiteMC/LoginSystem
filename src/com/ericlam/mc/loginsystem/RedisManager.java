package com.ericlam.mc.loginsystem;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {
    private static RedisManager redisManager;
    private String host;
    private Integer port;
    private int timeout;
    private String pw;
    private JedisPoolConfig config;
    private JedisPool pool;

    private RedisManager() {
        boolean bungee;
        try{
            Class.forName("net.md_5.bungee.BungeeCord");
            bungee = true;
        } catch (ClassNotFoundException e) {
            bungee = false;
        }
        config = new JedisPoolConfig();
        config.setMaxIdle(20);
        config.setMaxTotal(60);
        config.setMaxWaitMillis(7000);
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        config.setMinEvictableIdleTimeMillis(500);
        config.setSoftMinEvictableIdleTimeMillis(1000);
        config.setTimeBetweenEvictionRunsMillis(1000);
        config.setNumTestsPerEvictionRun(100);
        try {
            RedisData data = ReflectionRedis.getRedisData(bungee);
            host = data.getIp();
            port = data.getPort();
            pw = data.getPassword();
            timeout = data.getTimeout() * 1000;
            boolean usePassword = data.isUsePassword();
            if (usePassword) pool = new JedisPool(config, host, port, timeout, pw, 1);
            else pool = new JedisPool(config, host, port, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RedisManager getInstance() {
        if (redisManager == null) redisManager = new RedisManager();
        return redisManager;
    }

    public Jedis getRedis() {
        return pool.getResource();
    }

    public void closePool() {
        if (pool == null) return;
        pool.close();
    }
}
