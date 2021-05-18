package com.jtheories.examples;

import com.jtheories.junit.JTheoriesExtension;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This class exists to test library performance in local environments and it's not intended as a real test class
 */
@Disabled
@ExtendWith(JTheoriesExtension.class)
class LoadTest {

	@RepeatedTest(Integer.MAX_VALUE)
	void loadTest(Collection<VeryRealClass> veryRealObjects) {
		Assertions.assertNotNull(veryRealObjects);
	}
}
