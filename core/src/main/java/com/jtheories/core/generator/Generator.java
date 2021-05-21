package com.jtheories.core.generator;

import com.jtheories.core.generator.meta.TypeArgument;

public interface Generator<T> {
	default T generateBasic() {
		throw new AssertionError("Wrong generate method invoked");
	}

	default T generateBasic(Class<T> type) {
		throw new AssertionError("Wrong generate method invoked");
	}

	T generate(TypeArgument<?> typeArgument);
}
