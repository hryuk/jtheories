package com.jtheories.core.generator.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValuedAnnotation {

	private final Class<? extends Annotation> annotation;
	private final Map<String, Object> values;

	public ValuedAnnotation(
		Class<? extends Annotation> annotation,
		Map<String, Object> values
	) {
		this.annotation = annotation;
		this.values = values;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static List<ValuedAnnotation> getValuedAnnotationsFrom(
		AnnotatedElement annotatedElement
	) {
		return Arrays
			.stream(annotatedElement.getAnnotations())
			.map(
				annotation -> {
					var annotationClass = annotation.annotationType();
					var annotationInstance = annotatedElement.getDeclaredAnnotation(
						annotationClass
					);
					var annotationValues = Arrays
						.stream(annotationClass.getDeclaredMethods())
						.collect(
							Collectors.toMap(
								Method::getName,
								method -> {
									try {
										return method.invoke(annotationInstance);
									} catch (IllegalAccessException | InvocationTargetException e) {
										throw new AssertionError(
											"Unexpected error calling annotation method"
										);
									}
								}
							)
						);
					return new ValuedAnnotation(annotationClass, annotationValues);
				}
			)
			.collect(Collectors.toList());
	}

	public Class<? extends Annotation> getAnnotation() {
		return this.annotation;
	}

	public Map<String, Object> getValues() {
		return this.values;
	}

	public static class Builder {

		private final Map<String, Object> values = new HashMap<>();
		private Class<? extends Annotation> annotation;

		public Builder annotation(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
			return this;
		}

		public Builder value(String name, Object value) {
			this.values.put(name, value);
			return this;
		}

		public ValuedAnnotation build() {
			return new ValuedAnnotation(this.annotation, this.values);
		}
	}
}
