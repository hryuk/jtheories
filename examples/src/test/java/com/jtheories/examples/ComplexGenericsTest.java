package com.jtheories.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComplexGenericsTest {

	@Test
	void testGenerateComplexGenerics(ComplexGenerics<Long, Boolean> complexGenerics) {
		Assertions.assertNotNull(complexGenerics);
	}
}
