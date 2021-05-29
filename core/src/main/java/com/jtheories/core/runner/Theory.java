package com.jtheories.core.runner;

import java.util.function.Consumer;

public interface Theory<T> {
	void check(final Consumer<T> property);
}
