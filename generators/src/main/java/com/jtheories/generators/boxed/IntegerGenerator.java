package com.jtheories.generators.boxed;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface IntegerGenerator {
	default Integer generate(SourceOfRandom random) {
		return random.getRandom().nextInt();
	}
}
