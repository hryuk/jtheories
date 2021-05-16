package com.jtheories.generators;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface StringGenerator {
	default String generate(SourceOfRandom random) {
		final var MAX_STRING_LENGTH = 2048L;

		return random
			.getRandom()
			.ints(0x0000, 0xD7FF)
			.limit(MAX_STRING_LENGTH)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
	}
}
