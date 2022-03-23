package com.jtheories.generators.number;

import com.jtheories.core.generator.meta.Constrain;
import com.jtheories.core.generator.meta.GeneratorAnnotations;
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

	@NotMultipleOf
	default Long generateNotMultipleOf(
		Long arbitraryLong,
		GeneratorAnnotations annotations
	) {
		long multiple = annotations.get(NotMultipleOf.class);
		if (arbitraryLong % multiple == 0) {
			return arbitraryLong + 1;
		}
		return arbitraryLong;
	}
}
