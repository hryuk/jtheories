package com.jtheories.examples;

import com.jtheories.core.generator.processor.Generator;

@Generator
public interface VeryRealClassGenerator {
	default VeryRealClass generate(
		Boolean aBoolean,
		Byte aByte,
		Character character,
		Short aShort,
		Integer integer,
		Long aLong,
		Float aFloat,
		Double aDouble
	) {
		return new VeryRealClass(
			aBoolean,
			aByte,
			character,
			aShort,
			integer,
			aLong,
			aFloat,
			aDouble
		);
	}
}
