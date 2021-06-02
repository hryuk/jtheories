package com.jtheories.examples;

import com.jtheories.core.runner.JTheories;
import com.jtheories.junit.JTheoriesParameterResolver;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesParameterResolver.class)
class ProductTest {

	@Disabled("Until constrains are added back")
	@Test
	void productHasPriceAndName() {
		JTheories
			.<Product>forAll()
			.check(
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

	@Disabled("Until constrains are added back")
	@Test
	void freeProductsCostNothing() {
		JTheories
			.<@Free Product>forAll()
			.check(product -> Assertions.assertEquals(0L, product.getPrice()));
	}

	@Disabled("Until constrains are added back")
	@Test
	void productsHavePriceAndName() {
		JTheories
			.<List<Product>>forAll()
			.check(
				products ->
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
					)
			);
	}

	@Test
	@Disabled("Until constrains are added back")
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

	@Test
	void productListHaveOrderPrice() {
		JTheories
			.<Collection<Product>>forAll()
			.check(
				products -> {
					Order order = new Order(products);
					Assertions.assertTrue(Objects.nonNull(order.getTotalPrice()));
				}
			);
	}

	@Test
	void commetDoesNotBreakThings() {
		JTheories
			// comment 1
			/*comment 2*/.<Collection</*comment 3*/Product>>/*coment 4*/forAll()
			//comment 5
			.check(
				products -> {
					System.out.println(products);
					Order order = new Order(products);
					Assertions.assertTrue(Objects.nonNull(order.getTotalPrice()));
				}
			);
	}
}
