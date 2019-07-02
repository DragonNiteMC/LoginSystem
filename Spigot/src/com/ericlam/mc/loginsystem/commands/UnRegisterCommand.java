package com.ericlam.mc.loginsystem.commands;

import com.ericlam.mc.loginsystem.exceptions.AuthException;
import com.ericlam.mc.loginsystem.managers.LoginManager;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UnRegisterCommand extends FutureAuthCommandNode {

    public UnRegisterCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "unregister", "取消註冊", null, "unreg");
    }

    @Override
    public CompletableFuture<Boolean> executeOperation(@Nonnull Player player, @Nonnull List<String> list) throws AuthException {
        if (loginManager.notLoggedIn(player.getUniqueId())){
            player.sendMessage(configManager.getMessage("not-logged-in"));
            return CompletableFuture.completedFuture(false);
        }
        return loginManager.unregister(player);
    }
}
