package com.jtheories.core.generator;

import java.util.ArrayList;
import java.util.List;

public interface Generator<T> {
	default T generateBasic() {
		throw new AssertionError("Wrong generate method invoked");
	}

	default T generateBasic(Class<T> type) {
		throw new AssertionError("Wrong generate method invoked");
	}

	T generate(List<TypeArgument> typeArguments);
}
