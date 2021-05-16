package com.jtheories.generators.number;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface LongGenerator {
	default Long generate(SourceOfRandom random) {
		return random.getRandom().nextLong();
	}

	@Positive
	default Long generatePositive(SourceOfRandom random) {
		return (long) (random.getRandom().nextFloat() * (Long.MAX_VALUE - 1));
	}

	@NotMultipleOf10
	default Long generateNotMultipleOf10(Long arbitraryLong) {
		if (arbitraryLong % 10 == 0) {
			return arbitraryLong + 1;
		}
		return arbitraryLong;
	}
}
