package com.jtheories.examples;

import java.util.Collection;
import java.util.List;

public class Order {
  private final Collection<Product> items;
  private Long totalPrice = 0L;

  public Order(List<Product> items) {
    this.items = items;
    items.stream().map(Product::getPrice).forEach((price) -> totalPrice += price);
  }

  public Collection<Product> getItems() {
    return this.items;
  }

  public Long getTotalPrice() {
    return totalPrice;
  }
}
