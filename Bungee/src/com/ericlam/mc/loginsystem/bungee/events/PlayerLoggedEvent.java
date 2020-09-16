package com.ericlam.mc.loginsystem.bungee.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PlayerLoggedEvent extends Event implements Cancellable {

    private boolean cancel;
    private final ProxiedPlayer who;
    private final int failTimesBeforeLogin;
    private final long logonTime;
    private final LocalDateTime localDateTime;

    public PlayerLoggedEvent(ProxiedPlayer who, int failTimesBeforeLogin, long logonTime) {
        this.who = who;
        this.failTimesBeforeLogin = failTimesBeforeLogin;
        this.logonTime = logonTime;
        this.localDateTime = new Timestamp(logonTime).toLocalDateTime();
    }

    public ProxiedPlayer getWho() {
        return who;
    }

    public int getFailTimesBeforeLogin() {
        return failTimesBeforeLogin;
    }

    public long getLogonTime() {
        return logonTime;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
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
