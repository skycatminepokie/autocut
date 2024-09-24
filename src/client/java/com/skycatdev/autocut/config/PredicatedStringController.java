package com.skycatdev.autocut.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.StringController;

import java.util.function.Function;

public class PredicatedStringController extends StringController {
    private final Function<String, Boolean> predicate;
    public PredicatedStringController(Option<String> option, Function<String, Boolean> predicate) {
        super(option);
        this.predicate = predicate;
    }

    @Override
    public boolean isInputValid(String input) {
        return super.isInputValid(input) && predicate.apply(input);
    }
}
