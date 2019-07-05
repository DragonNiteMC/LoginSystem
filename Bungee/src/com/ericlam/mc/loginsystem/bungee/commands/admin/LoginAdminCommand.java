package com.ericlam.mc.loginsystem.bungee.commands.admin;

import com.ericlam.mc.bungee.hnmc.commands.caxerx.CommandNode;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommand;
import com.ericlam.mc.bungee.hnmc.commands.caxerx.DefaultCommandBuilder;
import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.permission.Perm;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;

public class LoginAdminCommand {

    private final DefaultCommandBuilder defaultCommand;

    public LoginAdminCommand(LoginManager loginManager, ConfigManager configManager) {
        this.defaultCommand = new DefaultCommandBuilder("loginadmin").permission(Perm.ADMIN).description("登入系統的管理員指令").alias("loginsys", "la");
        CommandNode editpw = new EditPasswordCommand(loginManager, configManager);
        CommandNode unreg = new UnregisterCommand(loginManager, configManager);
        CommandNode updateip = new UpdateIPCommand(loginManager, configManager);
        this.defaultCommand.children(editpw, unreg, updateip);
    }

    public DefaultCommand getDefaultCommand() {
        return this.defaultCommand.build();
    }
}
