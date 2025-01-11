package com.skycatdev.autocut.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;

import java.util.function.Consumer;
import java.util.function.Function;

public class LazyPredicatedStringController extends PredicatedStringController {
    public LazyPredicatedStringController(Option<String> option, Function<String, Boolean> predicate) {
        super(option, predicate);
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new LazyStringControllerElement(this, screen, widgetDimension, true);
    }

    public class LazyStringControllerElement extends StringControllerElement {

        public LazyStringControllerElement(IStringController<?> control, YACLScreen screen, Dimension<Integer> dim, boolean instantApply) {
            super(control, screen, dim, instantApply);
        }

        @Override
        public boolean modifyInput(Consumer<StringBuilder> consumer) {
            StringBuilder temp = new StringBuilder(inputField);
            consumer.accept(temp);
            inputField = temp.toString();
            if (instantApply)
                updateControl();
            return true;
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused && !isInputValid(inputField)) {
                modifyInput((builder) -> builder.delete(0, builder.length() - 1));
            }
            super.setFocused(focused);
        }

        @Override
        public void unfocus() {
            if (!isInputValid(inputField)) {
                modifyInput((builder) -> builder.delete(0, builder.length() - 1));
            }
            super.unfocus();
        }
    }
}
