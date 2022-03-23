package com.jtheories.core.generator.meta;

import java.util.List;

public class GeneratorAnnotations {

	public List<ValuedAnnotation> getAnnotations() {
		return annotations;
	}

	private final List<ValuedAnnotation> annotations;

	public GeneratorAnnotations(List<ValuedAnnotation> annotations) {
		this.annotations = annotations;
	}

	public <T> T get(Class<?> clazz, String value) {
		Object annotationValue =
			this.annotations.stream()
				.filter(a -> a.getAnnotation().equals(clazz))
				.findAny()
				.map(va -> va.getValues().get(value))
				.orElseThrow();

		return (T) annotationValue;
	}

	public <T> T get(Class<?> clazz) {
		return get(clazz, "value");
	}
}
