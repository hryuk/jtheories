package com.jtheories.generators;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;

@Generator
public interface CollectionGenerator<T> {
  default Collection<T> generate(Class<T> type) {
    Collection<T> generatedCollection = new ArrayList<>();
    IntStream.range(0, 100).forEach(i -> generatedCollection.add(Generators.gen(type)));
    return generatedCollection;
  }
}
