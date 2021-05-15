package com.jtheories.core.generator;

import java.lang.reflect.InvocationTargetException;

public interface Generator<T> {

  default T generate() {
    throw new AssertionError("Wrong generate method invoked");
  }

  default T generate(Class<T> type) {
    throw new AssertionError("Wrong generate method invoked");
  }

  // TODO: Remove exceptions from this method signature and throw a Runtime instead
  T generateConstrained(Class<?> type, Class<?>... annotations)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
