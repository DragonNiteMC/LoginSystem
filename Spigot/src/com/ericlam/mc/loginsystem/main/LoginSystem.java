package com.ericlam.mc.loginsystem.main;

import com.ericlam.mc.loginsystem.redis.ChannelListener;
import com.dragonnite.mc.dnmc.core.main.DragonNiteMC;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
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

    private final Set<UUID> uuids = new HashSet<>();
    private List<String> permissions;

    private LuckPerms luckPerms;

    public static boolean hasPermission(User user, Node perm) {
        return user.data().contains(perm, NodeEqualityPredicate.EXACT).asBoolean();
    }

    @Override
    public void onEnable() {
        permissions = DragonNiteMC.getAPI().getFactory().getConfigFactory(this).register("lobby.yml", LoginConfig.class).dump().getConfigAs(LoginConfig.class).premiumPermissions;
        luckPerms = LuckPermsProvider.get();
        this.getServer().getPluginManager().registerEvents(this, this);
        luckPerms.getEventBus().subscribe(UserLoadEvent.class, this::onUserLoad);
        Bukkit.getScheduler().runTaskAsynchronously(this, this::launchRedis);
    }

    private void launchRedis() {
        try (Jedis jedis = DragonNiteMC.getAPI().getRedisDataSource().getJedis()) {
            jedis.subscribe(new ChannelListener(this), "Login-Slave");
        } catch (JedisException e) {
            this.getLogger().log(Level.SEVERE, "無法連接到 Jedis: " + e.getLocalizedMessage());
            this.getLogger().log(Level.SEVERE, "一分鐘後再試。");
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, this::launchRedis, 20 * 60);
        }
    }

    private void onUserLoad(UserLoadEvent e) {
        if (!this.uuids.contains(e.getUser().getUniqueId())) return;
        boolean save = false;
        boolean success = true;
        for (String perm : permissions) {
            Node node = Node.builder(perm)
                    .value(true)
                    .withContext(DefaultContextKeys.SERVER_KEY, "global")
                    .build();
            if (!hasPermission(e.getUser(), node)) {
                save = true;
                success = e.getUser().data().add(node).wasSuccessful();
                var result = success ? "Successfully" : "Failure for";
                this.getLogger().info(result + " added permission `" + perm + "` to " + e.getUser().getUsername());
            }
        }
        if (save && success) {
            luckPerms.getUserManager().saveUser(e.getUser())
                    .whenComplete((v, ex) -> this.getLogger().info("Saved Completed."));
        }
        this.uuids.remove(e.getUser().getUniqueId());
    }

    public void gainPermission(UUID uuid) {
        this.getLogger().info("added " + uuid + " as premium queue");
        this.uuids.add(uuid);
    }


}
