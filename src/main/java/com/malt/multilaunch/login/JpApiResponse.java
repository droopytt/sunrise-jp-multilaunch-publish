package com.malt.multilaunch.login;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JpApiResponse implements APIResponse {
    private final boolean success;
    private final int errorCode;
    private final String token;

    @JsonCreator
    public JpApiResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("errorCode") int errorCode,
            @JsonProperty("token") String token) {
        this.success = success;
        this.errorCode = errorCode;
        this.token = token;
    }

    public boolean success() {
        return success;
    }

    public int errorCode() {
        return errorCode;
    }

    @Override
    public String cookie() {
        return token;
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
