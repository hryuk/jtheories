package com.jtheories.core.generator;

public interface Generator<T> {
	default T generateBasic() {
		throw new AssertionError("Wrong generate method invoked");
	}

	default T generateBasic(Class<T> type) {
		throw new AssertionError("Wrong generate method invoked");
	}

	T generate(TypeArgument<?> typeArgument);
}
