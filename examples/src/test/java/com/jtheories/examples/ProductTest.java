package com.jtheories.examples;

import com.jtheories.junit.JTheoriesExtension;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesExtension.class)
class ProductTest {

  @RepeatedTest(200)
  void doSomethingTest(Product product) {
    Assertions.assertEquals(product.getId(), UUID.fromString(product.getId().toString()));
    Assertions.assertTrue(product.getPrice() > 0);
    Assertions.assertNotNull(product.getName());
    Assertions.assertNotEquals(product.getPrice() % 10, 0);
  }
}
