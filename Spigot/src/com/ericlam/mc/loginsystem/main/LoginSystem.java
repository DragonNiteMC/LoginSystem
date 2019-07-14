package com.ericlam.mc.loginsystem.main;

import com.ericlam.mc.loginsystem.RedisManager;
import com.ericlam.mc.loginsystem.redis.ChannelListener;
import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.event.user.UserLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.logging.Level;

public class LoginSystem extends JavaPlugin implements Listener {

    private List<String> playersCommand;

    private Set<UUID> uuids = new HashSet<>();

    @Override
    public void onEnable() {
        ConfigSetter setter = new ConfigSetter(this, "lobby.yml") {

            @Override
            public void loadConfig(Map<String, FileConfiguration> map) {
                FileConfiguration lang = map.get("lobby.yml");
                playersCommand = lang.getStringList("premium-permissions");
            }
        };
        HyperNiteMC.getAPI().registerConfig(setter);
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskAsynchronously(this, ()->{
            try(Jedis jedis = RedisManager.getInstance().getRedis()){
                jedis.subscribe(new ChannelListener(this), "Login-Slave");
            }catch (JedisException e){
                this.getLogger().log(Level.SEVERE, "無法連接到 Jedis: "+e.getLocalizedMessage());
            }
        });

        LuckPerms.getApi().getEventBus().subscribe(UserLoadEvent.class, this::onUserLoad);
    }

    private void onUserLoad(UserLoadEvent e) {
        LuckPermsApi api = LuckPerms.getApi();
        if (!this.uuids.contains(e.getUser().getUuid())) return;
        boolean save = false;
        for (String perm : playersCommand) {
            Node node = api.getNodeFactory().newBuilder(perm).setValue(true).setServer("global").build();
            if (!e.getUser().hasPermission(node).asBoolean()) {
                save = true;
                e.getUser().setPermission(node);
                this.getLogger().info("adding permission `" + node.getPermission() + "` to " + e.getUser().getName());
            }
        }
        if (save) {
            api.getUserManager().saveUser(e.getUser())
                    .thenComposeAsync(v -> e.getUser().refreshCachedData())
                    .whenComplete((v, ex) -> this.getLogger().info("Adding Completed."));
        }
        this.uuids.remove(e.getUser().getUuid());
    }

    public void gainPermission(UUID uuid) {
        this.getLogger().info("added " + uuid + " as premium queue");
        this.uuids.add(uuid);
    }


}
