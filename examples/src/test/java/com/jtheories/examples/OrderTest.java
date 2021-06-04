package com.jtheories.examples;

import static com.jtheories.core.runner.JTheories.theory;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OrderTest {

	@Test
	void orderPriceIsAccurate() {
		theory()
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
