package com.jtheories.core.runner;

import com.jtheories.core.generator.meta.TypeArgument;

public class JTheories {

	private JTheories() {}

	@SuppressWarnings("unchecked")
	public static <T> Theory<T> forAll() {
		throw new RuntimeException("Unexpected call to placeholder forAll");
	}

	@SuppressWarnings("unchecked")
	public static <T> Theory<T> forAll(TypeArgument<?> typeArgument) {
		return null;
		//return new Theory<>((T) Generators.gen(typeArgument));

	}
}
