package com.jtheories.generators.boxed;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface ShortGenerator {
	default Short generate(SourceOfRandom random) {
		var bytes = new byte[Short.BYTES];
		random.getRandom().nextBytes(bytes);
		return (short) ((bytes[0] << 8) + (bytes[1] & 0xff));
	}
}
