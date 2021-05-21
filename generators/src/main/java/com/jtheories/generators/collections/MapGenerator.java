package com.jtheories.generators.collections;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Generator
public interface MapGenerator {
	int MAX_SIZE = 1024;

	default <K, V> Map<K, V> generate(
		SourceOfRandom random,
		Supplier<K> keys,
		Supplier<V> values
	) {
		return IntStream
			.range(0, random.getRandom().nextInt(MAX_SIZE))
			.boxed()
			.collect(Collectors.toMap(i -> keys.get(), i -> values.get()));
	}
}
