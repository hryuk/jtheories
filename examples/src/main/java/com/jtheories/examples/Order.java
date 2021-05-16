package com.jtheories.examples;

import java.util.Collection;

public class Order {

	private final Collection<Product> items;
	private Long totalPrice = 0L;

	public Order(final Collection<Product> items) {
		this.items = items;
		items.stream().map(Product::getPrice).forEach(price -> this.totalPrice += price);
	}

	public Collection<Product> getItems() {
		return this.items;
	}

	public Long getTotalPrice() {
		return this.totalPrice;
	}
}
