package com.ericlam.mc.loginsystem.bungee.main;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import com.ericlam.mc.loginsystem.bungee.LoginConfig;
import com.ericlam.mc.loginsystem.bungee.LoginListeners;
import com.ericlam.mc.loginsystem.bungee.commands.EditPasswordCommand;
import com.ericlam.mc.loginsystem.bungee.commands.LoginCommand;
import com.ericlam.mc.loginsystem.bungee.commands.RegisterCommand;
import com.ericlam.mc.loginsystem.bungee.commands.UnRegisterCommand;
import com.ericlam.mc.loginsystem.bungee.commands.admin.LoginAdminCommand;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

public class LoginSystem extends Plugin implements Listener {

    @Override
    public void onEnable() {
        ConfigManager configManager;
        try {
            configManager = HyperNiteMC.getAPI().registerConfig(new LoginConfig(this));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        configManager.setMsgConfig("lang.yml", "prefix");
        LoginManager loginManager = new LoginManager(this, configManager);
        this.getProxy().getPluginManager().registerListener(this, new LoginListeners(this, configManager, loginManager));
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(this, new LoginCommand(loginManager, configManager));
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(this, new RegisterCommand(loginManager, configManager));
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(this, new UnRegisterCommand(loginManager, configManager));
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(this, new EditPasswordCommand(loginManager, configManager));
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(this, new LoginAdminCommand(loginManager, configManager).getDefaultCommand());
    }
}
