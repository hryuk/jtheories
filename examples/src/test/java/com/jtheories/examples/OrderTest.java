package com.jtheories.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jtheories.junit.JTheoriesParameterResolver;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesParameterResolver.class)
class OrderTest {

	@RepeatedTest(200)
	void orderPriceIsAccurate(final Order order) {
		final Long expectedTotal = order
			.getItems()
			.stream()
			.map(Product::getPrice)
			.reduce(0L, Long::sum);
		assertEquals(expectedTotal, order.getTotalPrice());
	}
}
