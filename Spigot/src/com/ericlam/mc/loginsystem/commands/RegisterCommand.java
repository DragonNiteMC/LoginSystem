package com.ericlam.mc.loginsystem.commands;

import com.ericlam.mc.loginsystem.exceptions.AuthException;
import com.ericlam.mc.loginsystem.managers.LoginManager;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RegisterCommand extends FutureAuthCommandNode{

    public RegisterCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "register", "註冊賬戶", "<password> <confirm password>", "reg");
    }

    @Override
    public CompletableFuture<Boolean> executeOperation(@Nonnull Player player, @Nonnull List<String> list) throws AuthException {
        final String pw = list.get(0);
        final String confirm = list.get(1);
        return loginManager.register(player, pw, confirm);
    }
}
