package com.jtheories.examples;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.generators.number.Positive;

import java.util.UUID;

@Generator
public interface ProductGenerator {
  default Product generate(UUID id, String name, @Positive Long price) {
    return new Product(id, name, price);
  }
}
