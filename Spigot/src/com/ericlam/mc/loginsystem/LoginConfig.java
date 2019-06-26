package com.ericlam.mc.loginsystem;

import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import com.hypernite.mc.hnmc.core.config.Extract;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class LoginConfig extends ConfigSetter {

    @Extract(name = "em")
    private int expireMins;

    @Extract(name = "tbf")
    private int timesBeforeFail;

    @Extract(name = "sbf")
    private int secBeforeFail;

    public LoginConfig(Plugin plugin) {
        super(plugin, "config.yml", "lang.yml");
    }

    @Override
    public void loadConfig(Map<String, FileConfiguration> map) {
        FileConfiguration config = map.get("config.yml");
        this.expireMins = config.getInt("session-expire");
        this.timesBeforeFail = config.getInt("times-failed-kick");
        this.secBeforeFail = config.getInt("sec-failed-kick");
    }
}
