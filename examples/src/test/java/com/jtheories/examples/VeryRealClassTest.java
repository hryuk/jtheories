package com.jtheories.examples;

import com.jtheories.junit.JTheoriesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesExtension.class)
class VeryRealClassTest {

	@Test
	void doSomethingTest(VeryRealClass veryRealClass) {
		Assertions.assertNotNull(veryRealClass);
	}
}
