package com.jtheories.core.generator;

public interface Generator<T> {
	default T generate() {
		throw new AssertionError("Wrong generate method invoked");
	}

	default T generate(Class<T> type) {
		throw new AssertionError("Wrong generate method invoked");
	}

	T generateConstrained(Class<?> type, Class<?>... annotations);
}
