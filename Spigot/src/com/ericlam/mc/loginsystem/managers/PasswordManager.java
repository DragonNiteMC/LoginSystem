package com.ericlam.mc.loginsystem.managers;

import com.ericlam.mc.loginsystem.RedisManager;
import com.ericlam.mc.loginsystem.exceptions.AccountNonExistException;
import com.ericlam.mc.loginsystem.exceptions.AlreadyRegisteredException;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.SQLDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class PasswordManager {

    private SQLDataSource sqlDataSource;

    public PasswordManager(Plugin plugin){
        this.sqlDataSource = HyperNiteMC.getAPI().getSQLDataSource();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            try(Connection connection = sqlDataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS `LoginData` (UUID VARCHAR(40) NOT NULL PRIMARY KEY, Name TINYTEXT NOT NULL, Password LONGTEXT NOT NULL )")) {
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private Map<UUID, String> passwordMap = new Hashtable<>();


    static String hashing(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] pw = password.getBytes();
        byte[] hashed = digest.digest(pw);
        return Base64.encodeBase64String(hashed);
    }

    public boolean register(@Nonnull OfflinePlayer player, final String password) throws AlreadyRegisteredException {
        try {
            String encoded = hashing(password);
            try (Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT IGNORE INTO `LoginData` VALUES  (?,?,?)")) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, player.getName());
                statement.setString(3, encoded);
                int result = statement.executeUpdate();
                if (result == 0) throw new AlreadyRegisteredException();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean editPassword(@Nonnull OfflinePlayer player, final String password) throws AccountNonExistException {
        try {
            final String encoded = hashing(password);
            try (Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE `LoginDaa` SET `Name`=?, `Password`=? WHERE `PlayerUUID`=?")) {
                statement.setString(1, player.getName());
                statement.setString(2, encoded);
                statement.setString(3, player.getUniqueId().toString());
                int result = statement.executeUpdate();
                if (result == 0) throw new AccountNonExistException();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean unregister(UUID uuid) throws AccountNonExistException {
        try (Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM `LoginData` WHERE `UUID`=?")) {
            statement.setString(1, uuid.toString());
            int result = statement.executeUpdate();
            if (result == 0) throw new AccountNonExistException();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nonnull
    public String getPasswordHash(UUID uuid){
        return Optional.ofNullable(passwordMap.get(uuid)).orElseGet(()-> {
            try {
                Optional<String> str =  this.getFromSQL(uuid).get();
                if (str.isEmpty()) Bukkit.getLogger().log(Level.SEVERE, "The password of "+uuid+" is null, maybe premium player? ");
                else return str.get();
            } catch (InterruptedException | ExecutionException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error while getting "+uuid+" password: "+e.getLocalizedMessage());
            }
            return "";
        });
    }

    public boolean needLogin(UUID uuid) {
        try (Jedis redis = RedisManager.getInstance().getRedis()) {
            return Boolean.parseBoolean(redis.hget(uuid.toString(), "premium"));
        }
    }


    public CompletableFuture<Optional<String>> getFromSQL(UUID uuid){
        return CompletableFuture.supplyAsync(()->{
            if (passwordMap.containsKey(uuid)){
                return Optional.of(passwordMap.get(uuid));
            }
            String result = null;
            try(Connection connection = sqlDataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT `Password` FROM `LoginData` WHERE `UUID`=?")){
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    String pwHash = set.getString("Password");
                    this.passwordMap.put(uuid, pwHash);
                    result = pwHash;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.ofNullable(result);
        });
    }
}
