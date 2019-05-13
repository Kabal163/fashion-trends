package com.epam.fashion.trends.core;

public enum AppOptions {
    RULES_FILE("rules"),
    HELP("help"),
    APP_ID("app_id"),
    CLIENT_SECRET("client_secret"),
    TOPIC("topic"),
    RECREATE_RULES("recreate_rules");


    private String name;

    AppOptions(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
