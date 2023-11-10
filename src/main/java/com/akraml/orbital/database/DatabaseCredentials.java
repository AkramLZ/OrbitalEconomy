package com.akraml.orbital.database;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class DatabaseCredentials {

    private final String host, username, password, database, dataSourceClassName;
    private final int port, maxPoolSize;

}
