package com.ericlam.mc.loginsystem.bungee.managers;


import com.ericlam.mc.bungee.dnmc.function.ResultParser;
import com.ericlam.mc.loginsystem.bungee.LoginConfig;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class SessionManager {
    private final long sessionMins;
    private final Map<UUID, LocalDateTime> sessionMap = new ConcurrentHashMap<>();

    SessionManager(LoginConfig config) {
        this.sessionMins = config.sessionExpireMins;
    }

    boolean isExpired(UUID uuid) {
        return ResultParser.check(() -> Optional.ofNullable(this.sessionMap.get(uuid)).map(ts -> ts.isBefore(LocalDateTime.now())).orElse(true)).ifTrue(() -> this.sessionMap.remove(uuid)).getResult();
    }

    void addSession(UUID uuid) {
        this.sessionMap.put(uuid, LocalDateTime.now().plusMinutes(sessionMins));
    }

    void clearSession(UUID uuid) {
        this.sessionMap.remove(uuid);
    }

}
