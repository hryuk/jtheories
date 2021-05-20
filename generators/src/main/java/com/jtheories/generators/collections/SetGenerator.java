package com.jtheories.generators.collections;

import com.jtheories.core.generator.processor.Generator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Generator
public interface SetGenerator {
	default <T> Set<T> generate(Collection<T> genericCollection) {
		return new HashSet<>(genericCollection);
	}
}
