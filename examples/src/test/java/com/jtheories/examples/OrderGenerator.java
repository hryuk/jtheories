package com.jtheories.examples;

import java.util.List;

// @Generator
public interface OrderGenerator {
  default Order generate(List<Product> items) {
    return new Order(items);
  }
}
