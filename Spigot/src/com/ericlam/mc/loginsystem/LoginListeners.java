package com.ericlam.mc.loginsystem;

import com.ericlam.mc.loginsystem.managers.PasswordManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class LoginListeners implements Listener {

    private PasswordManager passwordManager;

    public LoginListeners(Plugin plugin){
        this.passwordManager = new PasswordManager(plugin);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

    }
}
