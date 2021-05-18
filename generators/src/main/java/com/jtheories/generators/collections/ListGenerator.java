package com.jtheories.generators.collections;

import com.jtheories.core.generator.processor.Generator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Generator
public interface ListGenerator {
	default <T> List<T> generate(Collection<T> genericCollection) {
		return new ArrayList<>(genericCollection);
	}
}
