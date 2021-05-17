package com.jtheories.examples;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.generators.number.NotMultipleOf10;
import com.jtheories.generators.number.Positive;
import java.util.UUID;

@Generator
public interface ProductGenerator {
	default Product generate(UUID id, String name, @Positive @NotMultipleOf10 Long price) {
		return new Product(id, name, price);
	}

	@Free
	default Product generateFree(Product product) {
		return new Product(product.getId(), product.getName(), 0L);
	}
}
