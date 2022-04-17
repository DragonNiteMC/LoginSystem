package com.ericlam.mc.loginsystem.bungee.commands.admin;

import com.ericlam.mc.bungee.dnmc.config.YamlManager;
import com.ericlam.mc.bungee.dnmc.container.OfflinePlayer;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditPasswordCommand extends AdminAuthNode {
    EditPasswordCommand(LoginManager loginManager, YamlManager configManager) {
        super(loginManager, configManager, "editpassword", "修改密碼", "<password>", "editpw");
    }

    @Override
    public CompletableFuture<Boolean> execution(ProxiedPlayer player, List<String> list, OfflinePlayer target) throws AuthException {
        String pw = list.get(0);
        return loginManager.editPassword(target, pw).thenApply(bool -> {
            if (bool) loginManager.tryKick(target.getUniqueId(), "admin-kick.password-edited");
            return bool;
        });
    }
}
