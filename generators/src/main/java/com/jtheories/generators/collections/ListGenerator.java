package com.jtheories.generators.collections;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Generator
public interface ListGenerator<T> {
	default List<T> generate(Class<T> type, Class<T>... types) {
		var collectionGenerator = Generators.getGenerator(Collection.class);

		@SuppressWarnings("unchecked")
		Collection<T> generatedCollection = collectionGenerator.generate(type, types);

		return new ArrayList<>(generatedCollection);
	}
}
