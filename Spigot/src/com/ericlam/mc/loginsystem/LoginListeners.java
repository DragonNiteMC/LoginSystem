package com.ericlam.mc.loginsystem;

import com.ericlam.mc.loginsystem.managers.LoginManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class LoginListeners implements Listener {

    private LoginManager loginManager;

    public LoginListeners(Plugin plugin){
        this.loginManager = new LoginManager(plugin);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        loginManager.loadUserData(e.getPlayer().getUniqueId());
    }


}
