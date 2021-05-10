package com.jtheories.core.generator;

import java.lang.reflect.InvocationTargetException;

public interface Generator<T> {

  T generate();

  T generateConstrained(Class<?>... annotations)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
