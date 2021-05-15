package com.jtheories.generators.collections;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Generator
public interface ListGenerator<T> {

  default List<T> generate(Class<T> type, Class<T>... types)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    var collectionGenerator = Generators.getGenerator(Collection.class);

    @SuppressWarnings("unchecked")
    Collection<T> generatedCollection = collectionGenerator.generateConstrained(type, types);

    return new ArrayList<>(generatedCollection);
  }
}
