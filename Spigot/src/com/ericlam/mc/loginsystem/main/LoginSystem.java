package com.ericlam.mc.loginsystem.main;

import com.ericlam.mc.loginsystem.LoginConfig;
import com.ericlam.mc.loginsystem.LoginListeners;
import com.ericlam.mc.loginsystem.managers.LoginManager;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LoginSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigManager configManager = HyperNiteMC.getAPI().registerConfig(new LoginConfig(this));
        configManager.setMsgConfig("lang.yml","prefix");
        LoginManager loginManager = new LoginManager(this, configManager);
        this.getServer().getPluginManager().registerEvents(new LoginListeners(this, configManager, loginManager), this);
    }
}
