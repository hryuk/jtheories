package com.jtheories.examples;

import com.jtheories.core.runner.JTheories;
import com.jtheories.junit.JTheoriesParameterResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesParameterResolver.class)
class ComplexGenericsTest {

	@Test
	@SuppressWarnings("unused")
	void testGenerateComplexGenerics() {
		JTheories
			.<ComplexGenerics<Long, Boolean>>forAll()
			.check(
				complexGenerics -> {
					Assertions.assertNotNull(complexGenerics);
					Assertions.assertNotNull(complexGenerics.getA());
					// This checks that generated objects are actually of the expected types
					long l = complexGenerics.getA();
					boolean b = complexGenerics.getB() != null ? complexGenerics.getB() : false;
				}
			);
	}
}
