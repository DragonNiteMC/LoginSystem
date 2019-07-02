package com.ericlam.mc.loginsystem.commands;

import com.ericlam.mc.loginsystem.exceptions.AuthException;
import com.ericlam.mc.loginsystem.managers.LoginManager;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditPasswordCommand extends FutureAuthCommandNode {
    public EditPasswordCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "editpassword", "修改密碼", "<old password> <new password>", "editpw");
    }

    @Override
    public CompletableFuture<Boolean> executeOperation(@Nonnull Player player, @Nonnull List<String> list) throws AuthException {
        if (loginManager.notLoggedIn(player.getUniqueId())){
            player.sendMessage(configManager.getMessage("not-logged-in"));
            return CompletableFuture.completedFuture(false);
        }
        final String oldPw = list.get(0);
        final String newPw = list.get(1);
        return loginManager.editPassword(player, oldPw, newPw);
    }
}
