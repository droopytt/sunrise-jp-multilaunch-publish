package com.malt.multilaunch.ui.config;

import com.malt.multilaunch.model.Config;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.*;

abstract class ConfigField<T> {
    final String key;
    final String label;
    final Function<Config, T> getter;
    final BiConsumer<Config, T> setter;

    ConfigField(String key, String label, Function<Config, T> getter, BiConsumer<Config, T> setter) {
        this.key = key;
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    abstract JComponent createComponent(Config config);

    abstract void loadValue(Config config, JComponent component);

    abstract void saveValue(Config config, JComponent component);
}
