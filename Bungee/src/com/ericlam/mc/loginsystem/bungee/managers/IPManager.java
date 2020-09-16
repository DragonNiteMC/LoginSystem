package com.ericlam.mc.loginsystem.bungee.managers;

import com.ericlam.mc.bungee.hnmc.SQLDataSource;
import com.ericlam.mc.bungee.hnmc.main.HyperNiteMC;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IPManager {
    private final SQLDataSource dataSource;

    public IPManager() {
        this.dataSource = HyperNiteMC.getAPI().getSQLDataSource();
    }

    public static String getIP(net.md_5.bungee.api.connection.Connection connection) {
        return ((InetSocketAddress) connection.getSocketAddress()).getAddress().getHostAddress();
    }

    String updateIP(ProxiedPlayer player) {
        final String ip = getIP(player);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE `LoginData` SET `IP`=? WHERE `UUID`=?")) {
            statement.setString(1, ip);
            statement.setString(2, player.getUniqueId().toString());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ip;
    }

    int checkAccount(String ip) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `UUID`, `Name` FROM `LoginData` WHERE `IP`=?")) {
            statement.setString(1, ip);
            ResultSet set = statement.executeQuery();
            return set.getRow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
