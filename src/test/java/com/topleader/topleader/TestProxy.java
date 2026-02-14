package com.topleader.topleader;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestProxy<T> {

    private final T proxy;
    private final Map<String, List<Object[]>> invocations = new HashMap<>();

    @SuppressWarnings("unchecked")
    private TestProxy(Class<T> iface, Map<String, Object> stubs) {
        this.proxy = (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[]{iface}, (p, method, args) -> {
            invocations.computeIfAbsent(method.getName(), k -> new ArrayList<>()).add(args);
            var stub = stubs.get(method.getName());
            if (stub instanceof StubFunction fn) {
                return fn.apply(args);
            }
            return stub;
        });
    }

    public T proxy() {
        return proxy;
    }

    public List<Object[]> invocationsOf(String methodName) {
        return invocations.getOrDefault(methodName, List.of());
    }

    public boolean wasCalled(String methodName) {
        return !invocationsOf(methodName).isEmpty();
    }

    public boolean wasNotCalled(String methodName) {
        return invocationsOf(methodName).isEmpty();
    }

    public static <T> Builder<T> of(Class<T> iface) {
        return new Builder<>(iface);
    }

    @FunctionalInterface
    public interface StubFunction {
        Object apply(Object[] args);
    }

    public static class Builder<T> {
        private final Class<T> iface;
        private final Map<String, Object> stubs = new HashMap<>();

        Builder(Class<T> iface) {
            this.iface = iface;
        }

        public Builder<T> stub(String method, Object returnValue) {
            stubs.put(method, returnValue);
            return this;
        }

        public Builder<T> stub(String method, StubFunction fn) {
            stubs.put(method, fn);
            return this;
        }

        public TestProxy<T> build() {
            return new TestProxy<>(iface, stubs);
        }
    }
}
