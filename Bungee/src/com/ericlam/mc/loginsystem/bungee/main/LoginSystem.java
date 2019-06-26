package com.ericlam.mc.loginsystem.bungee.main;

import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.events.PlayerVerifyCompletedEvent;
import com.ericlam.mc.loginsystem.bungee.RedisHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LoginSystem extends Plugin implements Listener {

    private Set<UUID> cached = new HashSet<>();
    private RedisHandler handler;

    @Override
    public void onEnable() {
        this.handler = new RedisHandler();
    }


    @EventHandler
    public void onVerifiedCompleted(PlayerVerifyCompletedEvent e){
        OfflinePlayer player = e.getOfflinePlayer();
        if (cached.contains(player.getUniqueId())) return;
        CompletableFuture.supplyAsync(()->handler.commitPlayer(player.getUniqueId(), player.isPremium())).whenCompleteAsync((b,ex)->{
            if (b) cached.add(player.getUniqueId());
        });
    }

    @EventHandler
    public void onPlayerSwitch(ServerSwitchEvent e){
        ProxyServer.getInstance().getScheduler().runAsync(this, ()-> handler.passLogin(e.getPlayer().getUniqueId()));
    }
}
