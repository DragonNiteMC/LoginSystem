package com.ericlam.mc.loginsystem.bungee.managers;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class SessionManager {
    private final long sessionMins;
    private Map<UUID, LocalDateTime> sessionMap = new ConcurrentHashMap<>();

    SessionManager(ConfigManager configManager){
        this.sessionMins = configManager.getData("em", Integer.class).orElse(60);
    }

    boolean isExpired(UUID uuid){
        return Optional.ofNullable(this.sessionMap.get(uuid)).map(ts -> ts.isBefore(LocalDateTime.now())).orElse(true);
    }

    void addSession(UUID uuid){
        this.sessionMap.put(uuid, LocalDateTime.now().plusMinutes(sessionMins));
    }

    void addPremiumSession(UUID uuid) {
        this.sessionMap.put(uuid, LocalDateTime.of(9999, 12, 30, 12, 0));
    }

    void clearSession(UUID uuid){
        this.sessionMap.remove(uuid);
    }

}
