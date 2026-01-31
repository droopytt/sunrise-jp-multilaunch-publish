package com.malt.multilaunch.model;

import com.fasterxml.jackson.annotation.*;
import com.malt.multilaunch.hotkeys.KeyConstants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotkeyInfo {

    private int key;
    private boolean enabled;

    @JsonCreator
    public HotkeyInfo(@JsonProperty("key") int key, @JsonProperty("enabled") boolean enabled) {
        this.key = key;
        this.enabled = enabled;
    }

    @JsonGetter
    public int key() {
        return key;
    }

    public int nativeKey() {
        return KeyConstants.toNativeKeyCode(key);
    }

    @JsonGetter
    public boolean enabled() {
        return enabled;
    }

    @JsonSetter
    public void setKey(int key) {
        this.key = key;
    }

    @JsonSetter
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
