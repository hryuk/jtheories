package com.jtheories.examples;

import static com.jtheories.core.runner.JTheories.theory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VeryRealClassTest {

	@Test
	@SuppressWarnings("unused")
	void doSomethingTest() {
		theory()
			.<VeryRealClass>forAll()
			.check(
				veryRealClass -> {
					Assertions.assertNotNull(veryRealClass);
					// This checks that the objects are actually of the expected types
					byte aByte = veryRealClass.getAByte();
					double aDouble = veryRealClass.getADouble();
					float aFloat = veryRealClass.getAFloat();
					long aLong = veryRealClass.getALong();
					short aShort = veryRealClass.getAShort();
					char aChar = veryRealClass.getCharacter();
					int anInt = veryRealClass.getInteger();
				}
			);
	}
}
