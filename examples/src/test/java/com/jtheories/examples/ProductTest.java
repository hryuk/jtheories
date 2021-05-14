package com.jtheories.examples;

import com.jtheories.junit.JTheoriesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ExtendWith(JTheoriesExtension.class)
class ProductTest {

  @RepeatedTest(200)
  void productHasPriceAndName(Product product) {
    Assertions.assertEquals(product.getId(), UUID.fromString(product.getId().toString()));
    Assertions.assertTrue(product.getPrice() > 0);
    Assertions.assertNotNull(product.getName());
    Assertions.assertNotEquals(0, product.getPrice() % 10);
  }

  @Test
  void freeProductsCostNothing(@Free Product product) {
    Assertions.assertEquals(0L, product.getPrice());
  }

  @Test
  void productsHavePriceAndName(Collection<Product> products) {
    products.forEach(
        (product) -> {
          Assertions.assertEquals(product.getId(), UUID.fromString(product.getId().toString()));
          Assertions.assertTrue(product.getPrice() > 0);
          Assertions.assertNotNull(product.getName());
          Assertions.assertNotEquals(0, product.getPrice() % 10);
        });
  }

  @Test
  void productListHavePriceAndName(List<Product> products) {
    products.forEach(
        (product) -> {
          Assertions.assertEquals(product.getId(), UUID.fromString(product.getId().toString()));
          Assertions.assertTrue(product.getPrice() > 0);
          Assertions.assertNotNull(product.getName());
          Assertions.assertNotEquals(0, product.getPrice() % 10);
        });
  }
}
