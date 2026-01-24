package com.malt.multilaunch.launcher;

import static com.malt.multilaunch.launcher.launchers.jp.SunriseJpUltiLauncherModule.generateFormData;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LauncherLoginArgsTest {

    @Test
    public void testGeneratesCorrectFormDataWithSingleArg() {
        var formData = generateFormData("username", "password", Map.of("serverType", "Toontown Japan 2010"));
        var expected = "username=username&password=password&serverType=Toontown+Japan+2010";
        assertThat(formData).isEqualTo(expected);
    }

    @Test
    public void testGeneratesCorrectFormDataWithNoAdditionalArgs() {
        var formData = generateFormData("username", "password", Map.of());
        var expected = "username=username&password=password";
        assertThat(formData).isEqualTo(expected);
    }

    @Test
    public void testGeneratesCorrectFormDataWithMultipleArgs() {
        var formData = generateFormData(
                "username", "password", Map.of("serverType", "Toontown Japan 2010", "blah", "value testing"));
        var possibleValues = Set.of(
                "username=username&password=password&serverType=Toontown+Japan+2010&blah=value+testing",
                "username=username&password=password&blah=value+testing&serverType=Toontown+Japan+2010");
        assertThat(formData).isIn(possibleValues);
    }
}
