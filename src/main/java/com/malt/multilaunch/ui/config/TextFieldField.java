package com.malt.multilaunch.ui.config;

import com.malt.multilaunch.model.Config;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.*;

class TextFieldField extends ConfigField<String> {
    TextFieldField(String key, String label, Function<Config, String> getter, BiConsumer<Config, String> setter) {
        super(key, label, getter, setter);
    }

    @Override
    JComponent createComponent(Config config) {
        return new JTextField(10);
    }

    @Override
    void loadValue(Config config, JComponent component) {
        ((JTextField) component).setText(getter.apply(config));
    }

    @Override
    void saveValue(Config config, JComponent component) {
        setter.accept(config, ((JTextField) component).getText());
    }
}
