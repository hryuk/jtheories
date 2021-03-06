package com.jtheories.examples;

import java.util.UUID;

public class Product {

	private final UUID id;

	private final String name;

	private final Long price;

	public Product(UUID id, String name, Long price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Long getPrice() {
		return this.price;
	}

	@Override
	public String toString() {
		return (
			"Product{" +
			"id=" +
			this.id +
			", name='" +
			this.name +
			'\'' +
			", price=" +
			this.price +
			'}'
		);
	}
}
