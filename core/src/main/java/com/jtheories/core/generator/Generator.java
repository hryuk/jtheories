package com.jtheories.core.generator;

import java.lang.reflect.InvocationTargetException;

public interface Generator<T> {

  default T generate() {
    throw new RuntimeException();
  }

  default T generate(Class<T> type) {
    throw new RuntimeException();
  }

  T generateConstrained(Class<?>... annotations)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
