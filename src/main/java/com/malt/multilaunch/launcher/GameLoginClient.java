package com.malt.multilaunch.launcher;

import java.net.URI;
import java.util.Map;

public interface GameLoginClient<T> {
    T login(String username, String password, Map<String, String> additionalArgs);

    URI getLoginApiUri();

    Class<T> responseType();
}
