package com.malt.multilaunch.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Server {
    SUNRISE_JP("JP 2010"),
    SUNRISE_2004("Test 2004"),
    SUNRISE_BRAZIL("Brazil"),
    SUNRISE_TEST_2012("Test 2012"),
    SUNRISE_FINAL_2013("Final 2013");

    private final String canonicalName;
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    Server(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public static Server fromName(String name) {
        for (var server : Server.values()) {
            if (server.canonicalName.equals(name)) {
                return server;
            }
        }
        LOG.error("No server with canonical name %s".formatted(name));
        return SUNRISE_JP;
    }

    public String canonicalName() {
        return canonicalName;
    }
}
