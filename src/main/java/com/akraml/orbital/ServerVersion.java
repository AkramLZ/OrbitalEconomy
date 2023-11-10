package com.akraml.orbital;

import org.bukkit.Bukkit;

public enum ServerVersion {

    V1_8("1_8"),
    V1_9("1_9"),
    V1_10("1_10"),
    V1_11("1_11"),
    V1_12("1_12"),
    V1_13("1_13"),
    V1_14("1_14"),
    V1_15("1_15"),
    V1_16("1_16"),
    V1_17("1_17"),
    V1_18("1_18"),
    V1_19("1_19"),
    V1_20("1_20");

    private final String key;

    ServerVersion(String key) {
        this.key = key;
    }

    public boolean isGreaterThanOrEqualTo(ServerVersion other) {
        return ordinal() >= other.ordinal();
    }

    public boolean isLessThanOrEqualTo(ServerVersion other) {
        return ordinal() <= other.ordinal();
    }

    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final ServerVersion MINECRAFT_VERSION = ServerVersion.build();

    private static ServerVersion build() {
        for (ServerVersion k : ServerVersion.values()) {
            if (ServerVersion.VERSION.contains(k.key)) {
                return k;
            }
        }
        return V1_19;
    }

    public static ServerVersion get() {
        return MINECRAFT_VERSION;
    }

}
