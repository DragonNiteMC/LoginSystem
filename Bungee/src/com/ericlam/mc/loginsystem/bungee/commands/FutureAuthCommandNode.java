package com.ericlam.mc.loginsystem.bungee.commands;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.YamlManager;
import com.ericlam.mc.loginsystem.bungee.exceptions.AuthException;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class FutureAuthCommandNode extends AuthCommandNode {
    public FutureAuthCommandNode(LoginManager loginManager, YamlManager configManager, String command, String description, String placeholder, String... alias) {
        super(loginManager, configManager, command, description, placeholder, alias);
    }

    @Override
    public void executeAuth(ProxiedPlayer player, List<String> list) throws AuthException {
        this.executeOperation(player,list).whenComplete(((aBoolean, throwable) -> {
            if (throwable instanceof AuthException) {
                throw (AuthException) throwable;
            }
            String path = "operation." + (aBoolean ? "success" : "failed");
            MessageBuilder.sendMessage(player, msg.get(path));
        }));
    }

    public abstract CompletableFuture<Boolean> executeOperation(ProxiedPlayer player, List<String> list) throws AuthException;
}
