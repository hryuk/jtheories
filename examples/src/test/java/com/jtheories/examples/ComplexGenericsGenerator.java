package com.jtheories.examples;

import com.jtheories.core.generator.processor.Generator;
import java.util.function.Supplier;

@Generator
public interface ComplexGenericsGenerator {
	default <A, B> ComplexGenerics<A, B> generate(A a, Supplier<B> bSupplier) {
		return new ComplexGenerics<>(a, bSupplier.get());
	}
}
