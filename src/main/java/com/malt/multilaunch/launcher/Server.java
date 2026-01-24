package com.malt.multilaunch.launcher;

public enum Server {
    SUNRISE_JP("Sunrise JP 2010"),
    SUNRISE_2004("Sunrise 2004"),
    SUNRISE_TEST_2012("Test 2012"),
    SUNRISE_FINAL_2013("Final 2013");

    private final String canonicalName;

    Server(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public static Server fromName(String name) {
        for (var server : Server.values()) {
            if (server.canonicalName.equals(name)) {
                return server;
            }
        }
        throw new IllegalArgumentException("No server with canonical name %s".formatted(name));
    }

    public String canonicalName() {
        return canonicalName;
    }
}
