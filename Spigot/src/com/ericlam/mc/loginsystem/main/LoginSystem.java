package com.ericlam.mc.loginsystem.main;

import com.ericlam.mc.loginsystem.RedisManager;
import com.ericlam.mc.loginsystem.redis.ChannelListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

public class LoginSystem extends JavaPlugin implements Listener {

    private Set<UUID> notloggedIn = new HashSet<>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskAsynchronously(this, ()->{
            try(Jedis jedis = RedisManager.getInstance().getRedis()){
                jedis.subscribe(new ChannelListener(), "Login-Slave");
            }catch (JedisException e){
                this.getLogger().log(Level.SEVERE, "無法連接到 Jedis: "+e.getLocalizedMessage());
            }
        });
    }

    public void addNotLogged(UUID uuid){
        this.notloggedIn.add(uuid);
    }

    public void removeNotLogged(UUID uuid){
        this.notloggedIn.add(uuid);
    }


}
