package com.malt.multilaunch.hotkeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.malt.multilaunch.ui.ActiveAccountManager;
import java.util.concurrent.atomic.AtomicReference;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HotkeyServiceTest {

    @Test
    public void testBasicHotkeys() {
        var mutatedState = new AtomicReference<>();
        var hotkeyService = HotkeyService.builder()
                .withHotkeyMapping(NativeKeyEvent.VC_R, () -> mutatedState.set(new Object()))
                .withHotkeyMapping(NativeKeyEvent.VC_S, () -> mutatedState.set(null))
                .withActiveAccountManager(ActiveAccountManager.create())
                .build();

        // given
        var rKeyEvent = createKeyEventFor(NativeKeyEvent.VC_R);

        // when
        hotkeyService.handleKey(rKeyEvent);

        // then
        assertThat(mutatedState.get()).isNotNull();

        // given
        var sKeyEvent = createKeyEventFor(NativeKeyEvent.VC_S);

        // when
        hotkeyService.handleKey(sKeyEvent);

        // then
        assertThat(mutatedState.get()).isNull();
    }

    private static NativeKeyEvent createKeyEventFor(int key) {
        var rKeyEvent = Mockito.mock(NativeKeyEvent.class);
        when(rKeyEvent.getKeyCode()).thenReturn(key);
        return rKeyEvent;
    }
}
