package com.ericlam.mc.loginsystem.managers;

import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.SQLDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
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
