package com.jtheories.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jtheories.core.runner.JTheories;
import org.junit.jupiter.api.Test;

class OrderTest {

	@Test
	void orderPriceIsAccurate() {
		JTheories
			.<Order>forAll()
			.check(
				order -> {
					final Long expectedTotal = order
						.getItems()
						.stream()
						.map(Product::getPrice)
						.reduce(0L, Long::sum);
					assertEquals(expectedTotal, order.getTotalPrice());
				}
			);
	}
}
