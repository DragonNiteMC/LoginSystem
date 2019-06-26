package com.ericlam.mc.loginsystem.managers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SessionManager {
    private Map<UUID, Timestamp> sessionMap = new Hashtable<>();

    public boolean isExpired(UUID uuid){
        return Optional.ofNullable(this.sessionMap.get(uuid)).map(ts->ts.after(Timestamp.from(Instant.now()))).orElse(true);
    }

    public void addSession(UUID uuid){
        this.sessionMap.put(uuid, Timestamp.from(Instant.now()));
    }

    public void clearSession(UUID uuid){
        this.sessionMap.remove(uuid);
    }

}
