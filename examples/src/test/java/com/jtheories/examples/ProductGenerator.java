package com.jtheories.examples;

import com.jtheories.core.generator.meta.GeneratorAnnotations;
import com.jtheories.core.generator.processor.Generator;
import com.jtheories.generators.number.NotMultipleOf;
import com.jtheories.generators.number.Positive;
import java.util.UUID;

@Generator
public interface ProductGenerator {
	default Product generate(
		UUID id,
		String name,
		@Positive @NotMultipleOf(10) Long price
	) {
		return new Product(id, name, price);
	}

	@Free
	default Product free(Product product) {
		return new Product(product.getId(), product.getName(), 0L);
	}

	@Price
	default Product price(Product product, GeneratorAnnotations annotations) {
		return new Product(product.getId(), product.getName(), annotations.get(Price.class));
	}

	@Name
	default Product name(Product product, GeneratorAnnotations annotations) {
		return new Product(product.getId(), annotations.get(Name.class), product.getPrice());
	}
}
