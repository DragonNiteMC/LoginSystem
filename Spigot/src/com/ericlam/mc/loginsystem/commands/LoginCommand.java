package com.ericlam.mc.loginsystem.commands;

import com.ericlam.mc.loginsystem.exceptions.AuthException;
import com.ericlam.mc.loginsystem.managers.LoginManager;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import com.hypernite.mc.hnmc.core.misc.commands.CommandNode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;

public class LoginCommand extends AuthCommandNode {
    public LoginCommand(LoginManager loginManager, ConfigManager configManager) {
        super(loginManager, configManager, "login", "登入伺服器", "<password>", "l");
    }

    @Override
    public boolean executeAuth(@Nonnull Player player, @Nonnull List<String> list) throws AuthException {
        String password = list.get(0);
        loginManager.login(player, password);
        return true;
    }
}
