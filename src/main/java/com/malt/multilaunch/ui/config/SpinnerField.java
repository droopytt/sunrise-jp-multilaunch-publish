package com.malt.multilaunch.ui.config;

import com.malt.multilaunch.model.Config;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.*;

class SpinnerField extends ConfigField<Integer> {
    private final int min, max;

    SpinnerField(
            String key,
            String label,
            int min,
            int max,
            Function<Config, Integer> getter,
            BiConsumer<Config, Integer> setter) {
        super(key, label, getter, setter);
        this.min = min;
        this.max = max;
    }

    @Override
    JComponent createComponent(Config config) {
        SpinnerNumberModel model = new SpinnerNumberModel(0, min, max, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0"));
        return spinner;
    }

    @Override
    void loadValue(Config config, JComponent component) {
        ((JSpinner) component).setValue(getter.apply(config));
    }

    @Override
    void saveValue(Config config, JComponent component) {
        setter.accept(config, (Integer) ((JSpinner) component).getValue());
    }
}
