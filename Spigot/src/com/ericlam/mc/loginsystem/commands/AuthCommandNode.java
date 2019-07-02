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

public abstract class AuthCommandNode extends CommandNode {

    protected LoginManager loginManager;
    protected ConfigManager configManager;

    public AuthCommandNode(LoginManager loginManager, ConfigManager configManager, @Nonnull String command, @Nonnull String description, String placeholder, String... alias) {
        super(null, command, null, description, placeholder, alias);
        this.loginManager = loginManager;
        this.configManager = configManager;
    }

    @Override
    public boolean executeCommand(@Nonnull CommandSender commandSender, @Nonnull List<String> list) {
        if (!(commandSender instanceof Player)){
            commandSender.sendMessage(HyperNiteMC.getAPI().getCoreConfig().getNotPlayer());
            return true;
        }
        Player player = (Player) commandSender;
        try{
            return executeAuth(player, list);
        }catch (AuthException e){
            player.sendMessage(configManager.getMessage(e.getPath()));
        }
        return true;
    }

    public abstract boolean executeAuth(@Nonnull Player player, @Nonnull List<String> list) throws AuthException;

    @Override
    public List<String> executeTabCompletion(@Nonnull CommandSender commandSender, @Nonnull List<String> list) {
        return null;
    }
}
