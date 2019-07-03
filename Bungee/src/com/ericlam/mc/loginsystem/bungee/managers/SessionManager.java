package com.ericlam.mc.loginsystem.bungee.managers;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class SessionManager {
    private Map<UUID, Timestamp> sessionMap = new Hashtable<>();

    private int sessionMins;

    SessionManager(ConfigManager configManager){
        this.sessionMins = configManager.getData("em", Integer.class).orElse(60);
    }

    boolean isExpired(UUID uuid){
        return Optional.ofNullable(this.sessionMap.get(uuid)).map(ts->ts.after(Timestamp.from(Instant.now()))).orElse(true);
    }

    void addSession(UUID uuid){
        this.sessionMap.put(uuid, Timestamp.from(Instant.now().plusSeconds(60 * sessionMins)));
    }

    void clearSession(UUID uuid){
        this.sessionMap.remove(uuid);
    }

}
