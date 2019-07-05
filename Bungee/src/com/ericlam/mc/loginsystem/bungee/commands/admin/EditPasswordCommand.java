package com.ericlam.mc.loginsystem.bungee.commands.admin;

import com.ericlam.mc.bungee.hnmc.config.ConfigManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditPasswordCommand extends AdminAuthNode {
    EditPasswordCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "editpassword", "修改密碼", "<password>", "editpw");
    }

    @Override
    public CompletableFuture<Boolean> execution(ProxiedPlayer player, List<String> list, OfflinePlayer target) throws AuthException {
        String pw = list.get(0);
        return loginManager.editPassword(target, pw);
    }
}
