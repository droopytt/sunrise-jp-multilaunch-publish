package com.malt.multilaunch.servers;

public enum Server {
    SUNRISE_JP("Sunrise JP 2010"),
    SUNRISE_2004("Sunrise 2004");

    private final String canonicalName;

    Server(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String canonicalName() {
        return canonicalName;
    }
}
