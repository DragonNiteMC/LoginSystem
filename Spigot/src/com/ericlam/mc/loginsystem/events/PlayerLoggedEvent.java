package com.ericlam.mc.loginsystem.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerLoggedEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList hander = new HandlerList();
    private boolean cancel;

    private boolean premium;

    public PlayerLoggedEvent(Player who, boolean premium) {
        super(who);
        this.premium = premium;
    }

    @Override
    public HandlerList getHandlers() {
        return hander;
    }

    public boolean isPremium() {
        return premium;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }
}
