package com.jtheories.examples;

import com.jtheories.junit.JTheoriesExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JTheoriesExtension.class)
class OrderTest {

  @Test
  void orderPriceIsAccurate(Order order) {
    Long expectedTotal = order.getItems().stream().map(Product::getPrice).reduce(0L, Long::sum);
    assertEquals(expectedTotal, order.getTotalPrice());
  }
}
