package com.first.project.common.function;

@FunctionalInterface
public interface CacheSelector<T> {
    T select() throws Exception;
}
