package com.ericlam.mc.loginsystem.commands;

import com.ericlam.mc.loginsystem.exceptions.AuthException;
import com.ericlam.mc.loginsystem.managers.LoginManager;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class FutureAuthCommandNode extends AuthCommandNode {
    public FutureAuthCommandNode(LoginManager loginManager, ConfigManager configManager, @Nonnull String command, @Nonnull String description, String placeholder, String... alias) {
        super(loginManager, configManager, command, description, placeholder, alias);
    }

    @Override
    public boolean executeAuth(@Nonnull Player player, @Nonnull List<String> list) throws AuthException {
        this.executeOperation(player,list).whenComplete(((aBoolean, throwable) -> {
            if (throwable != null){
                throwable.printStackTrace();
                return;
            }
            String path = "operation."+aBoolean;
            player.sendMessage(configManager.getMessage(path));
        }));
        return true;
    }
    public abstract CompletableFuture<Boolean> executeOperation(@Nonnull Player player, @Nonnull List<String> list) throws AuthException;
}
