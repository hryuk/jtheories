package com.jtheories.examples;

import com.jtheories.junit.JTheoriesExtension;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheoriesExtension.class)
class MapGeneratorTest {

	@RepeatedTest(10)
	void mapsAreGenerated(Map<String, Long> map) {
		Assertions.assertNotNull(map);
		map.forEach(
			(key, value) -> {
				Assertions.assertNotNull(key);
				Assertions.assertNotNull(value);
			}
		);
	}

	@RepeatedTest(10)
	void mapsAreGeneratedAsParameters(Collection<Map<String, Long>> collection) {
		Assertions.assertNotNull(collection);
		collection.forEach(
			map ->
				map.forEach(
					(key, value) -> {
						Assertions.assertNotNull(key);
						Assertions.assertNotNull(value);
					}
				)
		);
	}

	@RepeatedTest(10)
	void mapsAreGeneratedWithNestedGenerics(Map<String, List<String>> map) {
		map.forEach(
			(key, value) -> {
				Assertions.assertNotNull(key);
				Assertions.assertNotNull(value);
				value.forEach(Assertions::assertNotNull);
			}
		);
	}
}
