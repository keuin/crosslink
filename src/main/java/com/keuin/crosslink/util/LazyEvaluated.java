package com.keuin.crosslink.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class LazyEvaluated<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private T value = null;
    private final AtomicBoolean evaluated = new AtomicBoolean(false);

    public LazyEvaluated(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!evaluated.get()) {
            value = supplier.get();
            evaluated.set(true);
        }
        return value;
    }

}
