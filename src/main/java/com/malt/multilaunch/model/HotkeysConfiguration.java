package com.malt.multilaunch.model;

import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotkeysConfiguration {
    private final HotkeyInfo resetHotkey;
    private final HotkeyInfo snapHotkey;

    @JsonCreator
    public HotkeysConfiguration(
            @JsonProperty("resetHotkey") HotkeyInfo resetHotkey, @JsonProperty("snapHotkey") HotkeyInfo snapHotkey) {
        this.resetHotkey = resetHotkey;
        this.snapHotkey = snapHotkey;
    }

    public static HotkeysConfiguration createDefault() {
        return new HotkeysConfiguration(new HotkeyInfo(VK_R, true), new HotkeyInfo(VK_S, true));
    }

    @JsonGetter
    public HotkeyInfo resetHotkey() {
        return resetHotkey;
    }

    @JsonGetter
    public HotkeyInfo snapHotkey() {
        return snapHotkey;
    }
}
