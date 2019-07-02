package com.ericlam.mc.loginsystem;

import com.ericlam.mc.loginsystem.events.PlayerLoggedEvent;
import com.ericlam.mc.loginsystem.managers.LoginManager;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginListeners implements Listener {

    private final LoginManager loginManager;
    private final ConfigManager configManager;
    private final String notLoggedIn;
    private final Map<UUID, BukkitTask> timerTasks = new HashMap<>();
    private final Plugin plugin;

    public LoginListeners(Plugin plugin, ConfigManager configManager, LoginManager loginManager){
        this.plugin = plugin;
        this.loginManager = loginManager;
        this.configManager = configManager;
        this.notLoggedIn = configManager.getMessage("not-logged-in");;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogged(PlayerLoggedEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (e.isCancelled()){
            player.sendMessage(configManager.getMessage("logged-fail"));
        }else{
            this.clearTimer(uuid);
            player.sendMessage(configManager.getMessage("logged-in"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        loginManager.clearFail(e.getPlayer());
        this.clearTimer(e.getPlayer().getUniqueId());
    }

    private void clearTimer(UUID uuid){
        if (timerTasks.containsKey(uuid)){
            timerTasks.get(uuid).cancel();
            timerTasks.remove(uuid);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        loginManager.isPremium(uuid).whenComplete((premium, ex)->{
            if (premium && loginManager.notLoggedIn(uuid)){
                    loginManager.passLogin(player);
                    return;
            }else if (!premium){
                loginManager.loadUserData(uuid);
                if (loginManager.notLoggedIn(uuid)){
                    long ticks = configManager.getData("sbf", Integer.class).orElse(60) * 20;
                    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,()->{
                        player.kickPlayer(configManager.getPureMessage("kick-timeout"));
                    },ticks);
                    timerTasks.put(uuid, task);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        if (loginManager.notLoggedIn(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage(notLoggedIn);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e){
        Player player = e.getPlayer();
        if (loginManager.notLoggedIn(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage(notLoggedIn);
        }
    }


}
