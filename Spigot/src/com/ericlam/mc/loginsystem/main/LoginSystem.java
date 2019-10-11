package com.ericlam.mc.loginsystem.main;

import com.ericlam.mc.loginsystem.RedisManager;
import com.ericlam.mc.loginsystem.redis.ChannelListener;
import com.hypernite.mc.hnmc.core.config.Prop;
import com.hypernite.mc.hnmc.core.config.yaml.Configuration;
import com.hypernite.mc.hnmc.core.config.yaml.Resource;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.event.user.UserLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class LoginSystem extends JavaPlugin implements Listener {

    private List<String> playersCommand;

    private Set<UUID> uuids = new HashSet<>();

    @Resource(locate = "lobby.yml")
    private static class LoginConfig extends Configuration{
        @Prop(path = "premium-permissions")
        private List<String> playersCommand;
    }

    @Override
    public void onEnable() {
        playersCommand = HyperNiteMC.getAPI().getFactory().getConfigFactory(this).register("lobby.yml", LoginConfig.class).dump().getConfigAs(LoginConfig.class).playersCommand;
        this.getServer().getPluginManager().registerEvents(this, this);
        LuckPerms.getApi().getEventBus().subscribe(UserLoadEvent.class, this::onUserLoad);
        Bukkit.getScheduler().runTaskAsynchronously(this, this::launchRedis);
    }

    private void launchRedis(){
        try(Jedis jedis = RedisManager.getInstance().getRedis()){
            jedis.subscribe(new ChannelListener(this), "Login-Slave");
        }catch (JedisException e){
            this.getLogger().log(Level.SEVERE, "無法連接到 Jedis: "+e.getLocalizedMessage());
            this.getLogger().log(Level.SEVERE, "一分鐘後再試。");
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, this::launchRedis, 20*60);
        }
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
