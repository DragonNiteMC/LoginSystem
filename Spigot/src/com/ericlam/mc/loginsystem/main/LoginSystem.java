package com.ericlam.mc.loginsystem.main;

import com.ericlam.mc.loginsystem.RedisManager;
import com.ericlam.mc.loginsystem.redis.ChannelListener;
import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.TemporaryMergeBehaviour;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.logging.Level;

public class LoginSystem extends JavaPlugin implements Listener {

    private Set<UUID> notloggedIn = new HashSet<>();

    private String notloggedInMessage;

    private List<String> playersCommand;

    @Override
    public void onEnable() {
        ConfigSetter setter = new ConfigSetter(this, "lang.yml") {

            @Override
            public void loadConfig(Map<String, FileConfiguration> map) {
                FileConfiguration lang = map.get("lang.yml");
                playersCommand = lang.getStringList("premium-permissions");
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


    public void gainPermission(UUID uuid) {
        LuckPermsApi api = LuckPerms.getApi();
        playersCommand.forEach(perm -> {
            Node node = api.getNodeFactory().newBuilder(perm).setNegated(false).build();
            api.getUserSafe(uuid).ifPresentOrElse(user -> user.setPermission(node, TemporaryMergeBehaviour.FAIL_WITH_ALREADY_HAS), () -> this.getLogger().warning("We cannot find user with uuid " + uuid.toString() + ", skipped."));
        });
    }


}
