package com.malt.multilaunch.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    private String name;
    private String username;

    @ToStringExclude
    private String password;

    private boolean wantLogin;

    @JsonCreator
    public Account(
            @JsonProperty("name") String name,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("wantLogin") boolean wantLogin) {
        this.name = StringUtils.isBlank(name) ? username : name;
        this.username = username;
        this.password = password;
        this.wantLogin = wantLogin;
    }

    public Account(String username, String password) {
        this(username, username, password, false);
    }

    @JsonGetter
    public String username() {
        return username;
    }

    @JsonGetter
    public String password() {
        return password;
    }

    @JsonGetter
    public String name() {
        return name;
    }

    @JsonGetter
    public boolean wantLogin() {
        return wantLogin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setWantLogin(boolean wantLogin) {
        this.wantLogin = wantLogin;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
