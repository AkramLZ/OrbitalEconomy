package com.akraml.orbital.database;

import com.akraml.orbital.user.EcoUser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@RequiredArgsConstructor
public final class DatabaseService {

    private final DatabaseCredentials credentials;
    private HikariDataSource dataSource;

    public boolean connect() {
        final HikariConfig hikariConfig = getHikariConfig();
        try {
            this.dataSource = new HikariDataSource(hikariConfig);

            try (final Connection connection = dataSource.getConnection();
                 final PreparedStatement statement = connection.prepareStatement(
                         "CREATE TABLE IF NOT EXISTS economy_data (id UUID PRIMARY KEY NOT NULL, name VARCHAR(16) UNIQUE, balance INT, last_earn BIGINT)"
                 )) {
                statement.executeUpdate();
                return true;
            }
        } catch (final Exception exception) {
            exception.printStackTrace(System.err);
            return false;
        }
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }

    private HikariConfig getHikariConfig() {
        final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDataSourceClassName(credentials.getDataSourceClassName());
        hikariConfig.setPoolName("orbital-economy");
        hikariConfig.setMaximumPoolSize(credentials.getMaxPoolSize());

        hikariConfig.addDataSourceProperty("serverName", credentials.getHost());
        hikariConfig.addDataSourceProperty("port", credentials.getPort());
        hikariConfig.addDataSourceProperty("databaseName", credentials.getDatabase());
        hikariConfig.addDataSourceProperty("user", credentials.getUsername());
        hikariConfig.addDataSourceProperty("password", credentials.getPassword());

        return hikariConfig;
    }

    public EcoUser getUser(final String name) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                "SELECT id, balance, last_earn FROM economy_data WHERE name = ?"
        )) {
            statement.setString(1, name.toLowerCase());
            try (final ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;
                return EcoUser.builder()
                        .uuid(UUID.fromString(resultSet.getString("id")))
                        .name(name.toLowerCase())
                        .balance(resultSet.getInt("balance"))
                        .lastEarn(resultSet.getLong("last_earn"))
                        .build();
            }
        } catch (final Exception exception) {
            exception.printStackTrace(System.err);
        }
        return null;
    }

    public EcoUser getUser(final UUID uuid) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                     "SELECT name, balance, last_earn FROM economy_data WHERE id = ?"
             )) {
            statement.setString(1, uuid.toString());
            try (final ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;
                return EcoUser.builder()
                        .uuid(uuid)
                        .name(resultSet.getString("name"))
                        .balance(resultSet.getInt("balance"))
                        .lastEarn(resultSet.getLong("last_earn"))
                        .build();
            }
        } catch (final Exception exception) {
            exception.printStackTrace(System.err);
        }
        return null;
    }

    public void updateUser(final EcoUser user) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO economy_data (id, name, balance, last_earn) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = ?, balance = ?, last_earn = ?"
        )) {
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getName());
            statement.setInt(3, user.getBalance());
            statement.setLong(4, user.getLastEarn());
            statement.setString(5, user.getName());
            statement.setInt(6, user.getBalance());
            statement.setLong(7, user.getLastEarn());
            statement.executeUpdate();
        } catch (final Exception exception) {
            exception.printStackTrace(System.err);
        }
    }

}
