package com.jtheories.examples;

import com.jtheories.junit.JTheoriesParameterResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.jtheories.core.runner.JTheories.theory;

@ExtendWith(JTheoriesParameterResolver.class)
class ProductTest {

	@Disabled("Until constrains are added back")
	@Test
	void productHasPriceAndName() {
		theory()
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
		theory()
			.<@Free Product>forAll()
			.check(product -> Assertions.assertEquals(0L, product.getPrice()));
	}

	@Disabled("Until constrains are added back")
	@Test
	void productsHavePriceAndName() {
		theory()
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
		theory()
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
		theory()
			.withSeed(2744503901576207320L)
			.withTrials(2L)/* Fails on 3rd trial */
			.<Collection<Product>>forAll()
			.check(
				products -> {
					Order order = new Order(products);
					Assertions.assertTrue(order.getTotalPrice() < 0);
					Assertions.assertTrue(Objects.nonNull(order.getTotalPrice()));
				}
			);
	}

	@Test
	void commetDoesNotBreakThings() {
		theory()
			// comment 1
			/*comment 2*/.<Collection</*comment 3*/Product>>/*coment 4*/forAll()
			//comment 5
			.check(
				products -> {
					Order order = new Order(products);
					Assertions.assertTrue(Objects.nonNull(order.getTotalPrice()));
				}
			);
	}
}
