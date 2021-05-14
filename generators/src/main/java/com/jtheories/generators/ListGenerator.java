package com.jtheories.generators;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;

import java.util.Collection;
import java.util.List;

@Generator
public interface ListGenerator<T> {
  default List<T> generate(Class<T> type) {
    @SuppressWarnings("unchecked")
    Collection<T> generatedCollection = Generators.gen(Collection.class, type);
    return List.copyOf(generatedCollection);
  }
}
