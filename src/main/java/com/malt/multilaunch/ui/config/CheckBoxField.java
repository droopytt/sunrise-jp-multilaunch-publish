package com.malt.multilaunch.ui.config;

import com.malt.multilaunch.model.Config;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.*;

class CheckBoxField extends ConfigField<Boolean> {
    CheckBoxField(String key, String label, Function<Config, Boolean> getter, BiConsumer<Config, Boolean> setter) {
        super(key, label, getter, setter);
    }

    @Override
    JComponent createComponent(Config config) {
        return new JCheckBox();
    }

    @Override
    void loadValue(Config config, JComponent component) {
        ((JCheckBox) component).setSelected(getter.apply(config));
    }

    @Override
    void saveValue(Config config, JComponent component) {
        setter.accept(config, ((JCheckBox) component).isSelected());
    }
}
