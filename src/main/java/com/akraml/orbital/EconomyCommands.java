package com.akraml.orbital;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.akraml.orbital.user.EcoUser;
import com.akraml.orbital.user.UsersManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class EconomyCommands extends BaseCommand {

    @Dependency
    private UsersManager usersManager;
    @Dependency
    private EconomyPlugin plugin;

    @CommandAlias("bal|balance")
    @Syntax("[player]")
    @Description("Shows yours or other player's balance.")
    public void onBalance(final Player player,
                          @Optional final String target) {
        final EcoUser user = usersManager.getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_NOT_LOADED_YET)));
            return;
        }
        if (user.isPerformingCommand()) {
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_COMMAND_PERFORM_INCOMPLETE)));
            return;
        }
        if (target == null) {
            // Check for self balance.
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_YOUR_BALANCE)
                    .replace("{balance}", String.valueOf(user.getBalance()))));
            return;
        }
        user.setPerformingCommand(true);
        CompletableFuture.runAsync(() -> {
            EcoUser targetUser = usersManager.getUser(target);
            if (targetUser == null) targetUser = plugin.getDatabaseService().getUser(target.toLowerCase());
            if (targetUser == null) {
                player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_PLAYER_NOT_FOUND)
                        .replace("{name}", target)));
                return;
            }
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_OTHERS_BALANCE)
                    .replace("{name}", target)
                    .replace("{balance}", String.valueOf(targetUser.getBalance()))));
        }).thenRun(() -> user.setPerformingCommand(false)).exceptionally(throwable -> {
            throwable.printStackTrace(System.err);
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_ERROR_OCCURRED)));
            user.setPerformingCommand(false);
            return null;
        });
    }

    @CommandAlias("pay")
    @Syntax("<player> <amount>")
    @Description("Pays the target a specific amount from your balance.")
    public void onPay(final Player player,
                      final String target,
                      final int amount) {
        final EcoUser user = plugin.getDatabaseService().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_NOT_LOADED_YET)));
            return;
        }
        if (user.isPerformingCommand()) {
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_COMMAND_PERFORM_INCOMPLETE)));
            return;
        }
        if (user.getBalance() < amount) {
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_INSUFFICIENT_FUNDS)));
            return;
        }
        user.setPerformingCommand(true);
        CompletableFuture.runAsync(() -> {
            EcoUser targetUser = usersManager.getUser(target);
            if (targetUser == null) targetUser = plugin.getDatabaseService().getUser(target);
            if (targetUser == null) {
                player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_PLAYER_NOT_FOUND)
                        .replace("{name}", target)));
                return;
            }
            targetUser.setBalance(amount);
            user.setBalance(user.getBalance() - amount);
            plugin.getDatabaseService().updateUser(targetUser);
            final Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null) {
                targetPlayer.sendMessage(color(
                        plugin.getConfig().getString(ConfigurationKeys.MESSAGES_PAYMENT_RECEIVED)
                                .replace("{amount}", String.valueOf(amount))
                                .replace("{player}", player.getName())
                ));
            }
            player.sendMessage(color(
                    plugin.getConfig().getString(ConfigurationKeys.MESSAGES_PAYMENT_SENT)
                            .replace("{amount}", String.valueOf(amount))
                            .replace("{player}", target)
            ));
        }).thenRun(() -> user.setPerformingCommand(false)).exceptionally(throwable -> {
            throwable.printStackTrace(System.err);
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_NOT_LOADED_YET)));
            user.setPerformingCommand(false);
            return null;
        });
    }

    @CommandAlias("setbal|setbalance")
    @CommandPermission("economy.setbalance")
    @Description("Updates the balance of the target.")
    @Syntax("<player> <balance>")
    public void onBalanceUpdate(final Player player,
                                final String target,
                                final int amount) {
        CompletableFuture.runAsync(() -> {
            EcoUser targetUser = usersManager.getUser(target);
            if (targetUser == null) targetUser = plugin.getDatabaseService().getUser(target);
            if (targetUser == null) {
                player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_PLAYER_NOT_FOUND)
                        .replace("{name}", target)));
                return;
            }
            targetUser.setBalance(amount);
            plugin.getDatabaseService().updateUser(targetUser);
            player.sendMessage(color(
                    plugin.getConfig().getString(
                            ConfigurationKeys.MESSAGES_BALANCE_UPDATED
                                    .replace("{player}", target)
                                    .replace("{balance}", String.valueOf(amount))
                    )
            ));
        }).exceptionally(throwable -> {
            throwable.printStackTrace(System.err);
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_NOT_LOADED_YET)));
            return null;
        });
    }

    @CommandAlias("earn")
    @Description("Earns a random balance between 1 and 5.")
    public void onEarn(final Player player) {
        final EcoUser user = plugin.getDatabaseService().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_NOT_LOADED_YET)));
            return;
        }
        if (user.isPerformingCommand()) {
            player.sendMessage(color(plugin.getConfig().getString(ConfigurationKeys.MESSAGES_COMMAND_PERFORM_INCOMPLETE)));
            return;
        }
        long deltaTimeMillis = System.currentTimeMillis() - user.getLastEarn();
        if (deltaTimeMillis < 60000L) {
            long remainTime = (60000L - deltaTimeMillis) / 1000;
            player.sendMessage(color(
                    plugin.getConfig().getString(
                            ConfigurationKeys.MESSAGES_EARN_COOLDOWN
                                    .replace("{time}", String.valueOf(remainTime))
                    )
            ));
            return;
        }
        int randomValue = ThreadLocalRandom.current().nextInt(4) + 1;
        user.setBalance(user.getBalance() + randomValue);
        player.sendMessage(color(
                plugin.getConfig().getString(
                        ConfigurationKeys.MESSAGES_EARNED
                                .replace("{amount}", String.valueOf(randomValue))
                )
        ));
    }

    private String color(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
