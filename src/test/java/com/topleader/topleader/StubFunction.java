package com.topleader.topleader;

import java.util.function.Function;

public class StubFunction<T, R> implements Function<T, R> {

    private final Function<T, R> defaultFn;
    private volatile Function<T, R> delegate;

    public StubFunction(Function<T, R> defaultFn) {
        this.defaultFn = defaultFn;
        this.delegate = defaultFn;
    }

    @Override
    public R apply(T t) {
        return delegate.apply(t);
    }

    public void returns(R value) {
        this.delegate = t -> value;
    }

    public void reset() {
        this.delegate = defaultFn;
    }
}
