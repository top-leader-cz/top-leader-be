/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.util.transaction;

/**
 * @author Daniel Slavik
 */
@FunctionalInterface
public interface TransactionProcess<T> {
    T execute();
}
