package com.akraml.orbital.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder @Getter @Setter
public final class EcoUser {

    private boolean performingCommand; // to prevent spamming commands until previous action is done.
    private final UUID uuid;
    private String name;
    private int balance;
    private long lastEarn;

}
