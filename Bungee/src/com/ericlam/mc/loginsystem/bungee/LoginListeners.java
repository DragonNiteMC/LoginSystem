package com.ericlam.mc.loginsystem.bungee;

import com.ericlam.mc.bungee.hnmc.builders.MessageBuilder;
import com.ericlam.mc.bungee.hnmc.config.YamlManager;
import com.ericlam.mc.bungee.hnmc.container.OfflinePlayer;
import com.ericlam.mc.bungee.hnmc.events.PlayerVerifyCompletedEvent;
import com.ericlam.mc.loginsystem.bungee.events.PlayerLoggedEvent;
import com.ericlam.mc.loginsystem.bungee.managers.IPManager;
import com.ericlam.mc.loginsystem.bungee.managers.LoginManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LoginListeners implements Listener {

    private final LoginManager loginManager;
    private final LoginConfig loginConfig;
    private final String notLoggedIn;
    private final Map<UUID, ScheduledTask> timerTasks = new ConcurrentHashMap<>();
    private final Map<String, String> loginIpMap = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private final RedisHandler redisHandler;
    private final LoginLang msg;
    private final Set<UUID> premiumPlayers = new HashSet<>();

    public LoginListeners(Plugin plugin, YamlManager configManager, LoginManager loginManager) {
        this.plugin = plugin;
        this.loginManager = loginManager;
        this.loginConfig = configManager.getConfigAs(LoginConfig.class);
        this.msg = configManager.getConfigAs(LoginLang.class);
        this.redisHandler = new RedisHandler();
        this.notLoggedIn = msg.get("not-logged-in");
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            timerTasks.keySet().forEach(uuid -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player == null) return;
                MessageBuilder.sendMessage(player, msg.get("need-login"));
                ProxyServer.getInstance().createTitle().subTitle(TextComponent.fromLegacyText(msg.get("need-login"))).fadeIn(20).fadeOut(20).stay(10).send(player);
            });
        }, 0, 10, TimeUnit.SECONDS);
    }


    private boolean isPremium(ProxiedPlayer player) {
        return premiumPlayers.contains(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogged(final PlayerLoggedEvent e) {
        ProxiedPlayer player = e.getWho();
        UUID uuid = player.getUniqueId();
        if (e.isCancelled()) {
            MessageBuilder.sendMessage(player, msg.get("logged-fail"));
        } else {
            this.clearTimer(uuid);
            MessageBuilder.sendMessage(player, msg.get("logged-in"));
        }
    }

    @EventHandler
    public void onServerConnect(final ServerConnectEvent e) {
        if (isPremium(e.getPlayer())) return;
        String lobby = loginConfig.lobby;
        if (!Optional.ofNullable(e.getPlayer().getServer()).map(Server::getInfo).map(ServerInfo::getName).orElse("").equals(lobby))
            return;
        if (e.getTarget().getName().equals(lobby)) return;
        if (loginManager.notLoggedIn(e.getPlayer())) {
            e.setCancelled(true);
            MessageBuilder.sendMessage(e.getPlayer(), notLoggedIn);
        }
    }

    @EventHandler
    public void onPlayerChat(final ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        if (isPremium(player)) return;
        String lobby = loginConfig.lobby;
        if (!player.getServer().getInfo().getName().equals(lobby)) return;
        if (loginManager.notLoggedIn(player)) {
            if (e.isProxyCommand() && (e.getMessage().startsWith("/login") || e.getMessage().startsWith("/register"))) {
                return;
            }
            plugin.getLogger().warning(player.getDisplayName() + " trying to type command " + e.getMessage() + " while not logged in.");
            e.setCancelled(true);
            MessageBuilder.sendMessage(player, notLoggedIn);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerDisconnectEvent e) {
        premiumPlayers.remove(e.getPlayer().getUniqueId());
        loginManager.clearFail(e.getPlayer());
        this.clearTimer(e.getPlayer().getUniqueId());
        this.loginIpMap.remove(e.getPlayer().getName());
    }

    private void clearTimer(UUID uuid) {
        if (timerTasks.containsKey(uuid)) {
            timerTasks.get(uuid).cancel();
            timerTasks.remove(uuid);
        }
    }

    @EventHandler
    public void onPlayerLogin(final PreLoginEvent e) {
        String ip = IPManager.getIP(e.getConnection());
        long amount = this.loginIpMap.values().stream().filter(address -> address.equals(ip)).count();
        if (amount >= loginConfig.maxLoginPerIP) {
            e.setCancelReason(new MessageBuilder(msg.getPure("max-login")).build());
            e.setCancelled(true);
            this.loginIpMap.forEach((k, v) -> {
                if (ProxyServer.getInstance().getPlayer(k) == null) this.loginIpMap.remove(k);
            });
            return;
        }
        this.loginIpMap.put(e.getConnection().getName(), ip);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerVerified(final PlayerVerifyCompletedEvent e) {
        e.registerIntent(plugin);
        OfflinePlayer player = e.getOfflinePlayer();
        if (!player.isPremium()) {
            loginManager.loadUserData(player.getUniqueId(), () -> e.completeIntent(plugin));
            return;
        }
        plugin.getLogger().info("Telling lobby to gain permission for " + player.getName());
        premiumPlayers.add(player.getUniqueId());
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> redisHandler.permissionGainPublish(player.getUniqueId()));
        e.completeIntent(plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PostLoginEvent e) {
        var player = e.getPlayer();
        loginManager.updateIPTask(player);
        if (isPremium(player)) {
            plugin.getLogger().info("player is premium, skipped login");
        } else {
            plugin.getLogger().info("player is not premium");
            if (loginManager.notLoggedIn(e.getPlayer())) {
                plugin.getLogger().info("player is not premium and not logged in.");
                long secs = loginConfig.secBeforeFail;
                ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                    player.disconnect(TextComponent.fromLegacyText(msg.getPure("kick-timeout")));
                }, secs, TimeUnit.SECONDS);
                timerTasks.put(player.getUniqueId(), task);
            }
        }
    }
}
