package com.jtheories.examples;

import com.jtheories.junit.JTheoriesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesExtension.class)
class VeryRealClassTest {

	@RepeatedTest(200)
	@SuppressWarnings("unused")
	void doSomethingTest(VeryRealClass veryRealClass) {
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
}
