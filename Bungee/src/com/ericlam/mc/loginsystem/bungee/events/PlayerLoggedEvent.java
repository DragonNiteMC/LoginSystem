package com.ericlam.mc.loginsystem.bungee.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class PlayerLoggedEvent extends Event implements Cancellable {

    private boolean cancel;
    private ProxiedPlayer who;
    private boolean premium;

    public PlayerLoggedEvent(ProxiedPlayer who, boolean premium) {
        this.who = who;
        this.premium = premium;
    }

    public boolean isPremium() {
        return premium;
    }

    public ProxiedPlayer getWho() {
        return who;
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
