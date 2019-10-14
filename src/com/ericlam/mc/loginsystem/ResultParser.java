package com.ericlam.mc.loginsystem;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ResultParser {
    private boolean result;

    private ResultParser(boolean result) {
        this.result = result;
    }

    public static ResultParser check(Supplier<Boolean> result) {
        return new ResultParser(result.get());
    }

    public ResultParser execute(Consumer<Boolean> resultRunner) {
        resultRunner.accept(result);
        return this;
    }

    public ResultParser ifTrue(Runnable runnable) {
        if (result) runnable.run();
        return this;
    }

    public ResultParser ifFalse(Runnable runnable) {
        if (!result) runnable.run();
        return this;
    }

    public boolean getResult() {
        return result;
    }

    public ResultParser setResult(Supplier<Boolean> result) {
        this.result = result.get();
        return this;
    }
}
