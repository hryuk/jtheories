package com.jtheories.examples;

import java.util.function.Supplier;

public interface ComplexGenericsGenerator {
	default <A, B> ComplexGenerics<A, B> generate(A a, Supplier<B> bSupplier) {
		return new ComplexGenerics<>(a, bSupplier.get());
	}
}
