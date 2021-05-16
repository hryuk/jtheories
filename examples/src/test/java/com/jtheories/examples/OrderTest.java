package com.jtheories.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jtheories.junit.JTheoriesExtension;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesExtension.class)
class OrderTest {

	@RepeatedTest(200)
	void orderPriceIsAccurate(Order order) {
		Long expectedTotal = order
				.getItems()
				.stream()
				.map(Product::getPrice)
				.reduce(0L, Long::sum);
		assertEquals(expectedTotal, order.getTotalPrice());
	}
}
