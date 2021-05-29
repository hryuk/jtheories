package com.jtheories.examples;

import com.jtheories.core.runner.JTheories;
import com.jtheories.junit.JTheoriesParameterResolver;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesParameterResolver.class)
class ProductTest {

	@Disabled("Until constrains are added back")
	@RepeatedTest(200)
	void productHasPriceAndName(Product product) {
		Assertions.assertEquals(product.getId(), UUID.fromString(product.getId().toString()));
		Assertions.assertTrue(product.getPrice() > 0);
		Assertions.assertNotNull(product.getName());
		Assertions.assertNotEquals(0, product.getPrice() % 10);
	}

	@Disabled("Until constrains are added back")
	@RepeatedTest(200)
	void freeProductsCostNothing(@Free Product product) {
		Assertions.assertEquals(0L, product.getPrice());
	}

	@Disabled("Until constrains are added back")
	@RepeatedTest(200)
	void productsHavePriceAndName(List<Product> products) {
		products.forEach(
			product -> {
				Assertions.assertEquals(
					product.getId(),
					UUID.fromString(product.getId().toString())
				);
				Assertions.assertTrue(product.getPrice() > 0);
				Assertions.assertNotNull(product.getName());
				Assertions.assertNotEquals(0, product.getPrice() % 10);
			}
		);
	}

	@Test
	void productListIsFree() {
		JTheories
			.<Collection<@Free Product>>forAll()
			.check(
				products -> {
					Order order = new Order(products);
					Assertions.assertEquals(0L, order.getTotalPrice());
				}
			);
	}
}
