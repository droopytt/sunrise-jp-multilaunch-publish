package com.malt.multilaunch.login;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RewrittenApiResponse implements APIResponse {

    private final String success;
    private final String message;
    private final String cookie;
    private final String gameserver;

    @JsonCreator
    public RewrittenApiResponse(
            @JsonProperty("success") String success,
            @JsonProperty("message") String message,
            @JsonProperty("cookie") String cookie,
            @JsonProperty("gameserver") String gameserver) {
        this.success = success;
        this.message = message;
        this.cookie = cookie;
        this.gameserver = gameserver;
    }

    @Override
    public String cookie() {
        return cookie;
    }

    public String success() {
        return success;
    }

    public String message() {
        return message;
    }

    public String gameserver() {
        return gameserver;
    }
}
