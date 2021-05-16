package com.jtheories.generators.collections;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Generator
public interface SetGenerator<T> {
	default Set<T> generate(Class<T> type, Class<T>... types) {
		var collectionGenerator = Generators.getGenerator(Collection.class);

		@SuppressWarnings("unchecked")
		Collection<T> generatedCollection = collectionGenerator.generate(type, types);

		return new HashSet<>(generatedCollection);
	}
}
