package com.jtheories.examples;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.generators.number.NotMultipleOf10;
import com.jtheories.generators.number.Positive;
import java.util.UUID;

@Generator
public interface ProductGenerator {
	// TODO: Why force creating a default implementation to simply call new?
	// Just call new by default and let the user provide a default implementation in case the created
	// object needs any special syntax to be constructed (i.e. a builder)
	default Product generate(UUID id, String name, @Positive @NotMultipleOf10 Long price) {
		return new Product(id, name, price);
	}

	@Free
	default Product generateFree(Product product) {
		return new Product(product.getId(), product.getName(), 0L);
	}
}
