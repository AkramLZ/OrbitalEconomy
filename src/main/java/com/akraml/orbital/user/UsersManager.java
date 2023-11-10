package com.akraml.orbital.user;

import com.akraml.orbital.EconomyPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public final class UsersManager {

    private final EconomyPlugin plugin;
    private final Map<UUID, EcoUser> userMap = new HashMap<>();

    public CompletableFuture<EcoUser> loadUser(final Player player) {
        return CompletableFuture.supplyAsync(() -> {
            EcoUser user = plugin.getDatabaseService().getUser(player.getUniqueId());
            if (user == null) {
                user = EcoUser.builder()
                        .uuid(player.getUniqueId())
                        .name(player.getName())
                        .balance(0)
                        .lastEarn(0L)
                        .build();
                plugin.getDatabaseService().updateUser(user);
            }
            return user;
        }).thenApply(user -> userMap.put(player.getUniqueId(), user)).exceptionally(throwable -> {
            throwable.printStackTrace(System.err);
            return null;
        });
    }

    public EcoUser getUser(final UUID uuid) {
        return userMap.get(uuid);
    }

    public EcoUser getUser(final String name) {
        return userMap.values().stream().filter(user -> user.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void unloadUser(final EcoUser user) {
        CompletableFuture.runAsync(() -> plugin.getDatabaseService().updateUser(user))
                .thenRun(() -> userMap.remove(user.getUuid()))
                .exceptionally(throwable -> { throwable.printStackTrace(System.err); return null; });
    }

}
