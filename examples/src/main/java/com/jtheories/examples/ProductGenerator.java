package com.jtheories.examples;

import static com.jtheories.core.generator.Generators.gen;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;
import java.util.UUID;

@Generator
public interface ProductGenerator {
  default Product generate(SourceOfRandom sourceOfRandom) {
    return new Product(gen(UUID.class), gen(String.class), gen(Long.class));
  }
}
