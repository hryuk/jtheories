package com.jtheories.generators.collections;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Generator
public interface SetGenerator<T> {
	default Set<T> generate(Collection<T> genericCollection) {
		return new HashSet<>(genericCollection);
	}
}
