package com.musigma.utils.exceptionMethods;

@FunctionalInterface
public interface Setter<T> {
    void accept(T t) throws Exception;
}