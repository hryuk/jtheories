package com.jtheories.examples;

import static com.jtheories.core.runner.JTheories.theory;

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

	@Test
	void freeProductsCostNothing() {
		theory()
			.<@Free Product>forAll()
			.check(product -> Assertions.assertEquals(0L, product.getPrice()));
	}

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
	void commentDoesNotBreakThings() {
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
