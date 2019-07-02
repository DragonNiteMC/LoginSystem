package com.ericlam.mc.loginsystem;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ResultParser {
    private Supplier<Boolean> result;

    private ResultParser(Supplier<Boolean> result) {
        this.result = result;
    }

    public static ResultParser check(Supplier<Boolean> result) {
        return new ResultParser(result);
    }

    public ResultParser execute(Consumer<Boolean> resultRunner) {
        resultRunner.accept(result.get());
        return this;
    }

    public ResultParser ifTrue(Runnable runnable) {
        if (result.get()) runnable.run();
        return this;
    }

    public ResultParser ifFalse(Runnable runnable) {
        if (!result.get()) runnable.run();
        return this;
    }

    public boolean getResult() {
        return result.get();
    }
}
