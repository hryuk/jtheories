package com.jtheories.examples;

import com.jtheories.junit.JTheoriesParameterResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesParameterResolver.class)
class ComplexGenericsTest {

	@RepeatedTest(10)
	@SuppressWarnings("unused")
	void testGenerateComplexGenerics(ComplexGenerics<Long, Boolean> complexGenerics) {
		Assertions.assertNotNull(complexGenerics);
		Assertions.assertNotNull(complexGenerics.getA());
		// This checks that generated objects are actually of the expected types
		long l = complexGenerics.getA();
		boolean b = complexGenerics.getB() != null ? complexGenerics.getB() : false;
	}
}
