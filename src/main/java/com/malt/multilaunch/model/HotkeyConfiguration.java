package com.malt.multilaunch.model;

import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotkeyConfiguration {

    private int resetHotkey;
    private int snapHotkey;

    @JsonCreator
    public HotkeyConfiguration(
            @JsonProperty("resetHotkey") int resetHotkey, @JsonProperty("snapHotkey") int snapHotkey) {
        this.resetHotkey = resetHotkey;
        this.snapHotkey = snapHotkey;
    }

    public static HotkeyConfiguration createDefault() {
        return new HotkeyConfiguration(VK_R, VK_S);
    }

    @JsonGetter("resetHotkey")
    public int resetHotkey() {
        return resetHotkey;
    }

    @JsonSetter("resetHotkey")
    public void setResetHotkey(int resetHotkey) {
        this.resetHotkey = resetHotkey;
    }

    @JsonGetter("snapHotkey")
    public int snapHotkey() {
        return snapHotkey;
    }

    @JsonSetter("snapHotkey")
    public void setSnapHotkey(int snapHotkey) {
        this.snapHotkey = snapHotkey;
    }
}
