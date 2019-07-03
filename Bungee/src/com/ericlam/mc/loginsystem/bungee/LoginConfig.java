package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.bungee.hnmc.config.ConfigSetter;
import com.ericlam.mc.bungee.hnmc.config.Extract;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;

public class LoginConfig extends ConfigSetter {

    @Extract(name = "em")
    private int expireMins;

    @Extract(name = "tbf")
    private int timesBeforeFail;

    @Extract(name = "sbf")
    private int secBeforeFail;

    @Extract
    private String lobby;

    public LoginConfig(Plugin plugin) {
        super(plugin, "config.yml", "lang.yml");
    }

    @Override
    public void loadConfig(HashMap<String, Configuration> hashMap) {
        Configuration config = hashMap.get("config.yml");
        this.expireMins = config.getInt("session-expire");
        this.timesBeforeFail = config.getInt("times-failed-kick");
        this.secBeforeFail = config.getInt("sec-failed-kick");
        this.lobby = config.getString("lobby");
    }
}
