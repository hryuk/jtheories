package com.jtheories.examples;

import com.jtheories.core.generator.processor.Generator;

import java.util.Collection;

@Generator
public interface OrderGenerator {
  default Order generate(final Collection<Product> items) {
    return new Order(items);
  }
}
