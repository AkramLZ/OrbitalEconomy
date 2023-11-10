package com.akraml.orbital;

import co.aikar.commands.BukkitCommandManager;
import com.akraml.orbital.database.DatabaseCredentials;
import com.akraml.orbital.database.DatabaseService;
import com.akraml.orbital.user.EcoUser;
import com.akraml.orbital.user.UsersManager;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class EconomyPlugin extends JavaPlugin implements Listener {

    private BukkitCommandManager commandManager;
    private DatabaseService databaseService;
    private final UsersManager usersManager = new UsersManager(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final DatabaseCredentials credentials = DatabaseCredentials.builder()
                .host(getConfig().getString(ConfigurationKeys.MYSQL_HOST))
                .port(getConfig().getInt(ConfigurationKeys.MYSQL_PORT))
                .database(getConfig().getString(ConfigurationKeys.MYSQL_DATABASE))
                .username(getConfig().getString(ConfigurationKeys.MYSQL_USERNAME))
                .password(getConfig().getString(ConfigurationKeys.MYSQL_PASSWORD))
                .dataSourceClassName(ServerVersion.get().isGreaterThanOrEqualTo(ServerVersion.V1_13)
                        ? "com.mysql.cj.jdbc.MysqlDataSource" : "com.mysql.jdbc.jdbc2.optional.MysqlDataSource")
                .maxPoolSize(getConfig().getInt(ConfigurationKeys.MYSQL_MAX_POOL_SIZE))
                .build();
        this.databaseService = new DatabaseService(credentials);
        if (!databaseService.connect()) {
            getLogger().severe("Failed to connect into database, disabling plugin...");
            setEnabled(false);
            return;
        }
        getLogger().info("Successfully connected into database!");

        commandManager = new BukkitCommandManager(this);
        commandManager.registerDependency(UsersManager.class, usersManager);
        commandManager.registerDependency(DatabaseService.class, databaseService);
        commandManager.registerCommand(new EconomyCommands());

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        databaseService.shutdown();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        usersManager.loadUser(event.getPlayer());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final EcoUser user = usersManager.getUser(event.getPlayer().getUniqueId());
        if (user == null) return;
        usersManager.unloadUser(user);
    }

}
