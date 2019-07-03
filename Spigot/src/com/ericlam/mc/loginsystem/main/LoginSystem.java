package com.ericlam.mc.loginsystem.main;

import com.ericlam.mc.loginsystem.RedisManager;
import com.ericlam.mc.loginsystem.redis.ChannelListener;
import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

public class LoginSystem extends JavaPlugin implements Listener {

    private Set<UUID> notloggedIn = new HashSet<>();

    private String notloggedInMessage;

    @Override
    public void onEnable() {
        ConfigSetter setter = new ConfigSetter(this, "lang.yml") {
            @Override
            public void loadConfig(Map<String, FileConfiguration> map) {
                //
            }
        };
        ConfigManager configManager = HyperNiteMC.getAPI().registerConfig(setter);
        configManager.setMsgConfig("lang.yml","prefix");
        this.notloggedInMessage = configManager.getMessage("not-logged-in");
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskAsynchronously(this, ()->{
            try(Jedis jedis = RedisManager.getInstance().getRedis()){
                jedis.subscribe(new ChannelListener(this), "Login-Slave");
            }catch (JedisException e){
                this.getLogger().log(Level.SEVERE, "無法連接到 Jedis: "+e.getLocalizedMessage());
            }
        });
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e){
        UUID uuid = e.getPlayer().getUniqueId();
        if (this.notloggedIn.contains(uuid)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(notloggedInMessage);
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        UUID uuid = e.getPlayer().getUniqueId();
        if (this.notloggedIn.contains(uuid)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(notloggedInMessage);
        }
    }

    public void addNotLogged(UUID uuid){
        this.notloggedIn.add(uuid);
    }

    public void removeNotLogged(UUID uuid){
        this.notloggedIn.add(uuid);
    }


}
