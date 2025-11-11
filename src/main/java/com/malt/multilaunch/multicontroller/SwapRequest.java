package com.malt.multilaunch.multicontroller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class SwapRequest {
    private final long hwnd1;
    private final long hwnd2;

    @JsonCreator
    public SwapRequest(@JsonProperty("hwnd1") long hwnd1, @JsonProperty("hwnd2") long hwnd2) {
        this.hwnd1 = hwnd1;
        this.hwnd2 = hwnd2;
    }

    @JsonGetter
    public long hwnd1() {
        return hwnd1;
    }

    @JsonGetter
    public long hwnd2() {
        return hwnd2;
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
