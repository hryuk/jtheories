package com.jtheories.core.generator.meta;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TypeArgument<T> {

	private final Class<T> type;
	private final List<ValuedAnnotation> annotations;
	private final TypeArgument<?>[] children;

	public TypeArgument(Class<T> type) {
		this.type = type;
		this.annotations = new ArrayList<>();
		this.children = new TypeArgument[0];
	}

	public TypeArgument(Class<T> type, List<ValuedAnnotation> annotations) {
		this.type = type;
		this.annotations = annotations;
		this.children = new TypeArgument[0];
	}

	public TypeArgument(Class<T> type, TypeArgument<?>[] children) {
		this.type = type;
		this.annotations = new ArrayList<>();
		this.children = children;
	}

	public TypeArgument(
		Class<T> type,
		List<ValuedAnnotation> annotations,
		TypeArgument<?>[] children
	) {
		this.type = type;
		this.annotations = annotations;
		this.children = children;
	}

	public static <S> TypeArgument<S> getTypeArgument(
		Class<S> typeClass,
		AnnotatedType annotatedType
	) {
		var annotations = ValuedAnnotation.getValuedAnnotationsFrom(annotatedType);

		TypeArgument<?>[] children = Stream
			.of(annotatedType)
			.filter(AnnotatedParameterizedType.class::isInstance)
			.map(AnnotatedParameterizedType.class::cast)
			.map(AnnotatedParameterizedType::getAnnotatedActualTypeArguments)
			.flatMap(Arrays::stream)
			.map(
				annotated -> {
					Class<?> childClass = obtainClass(annotated);
					return getTypeArgument(childClass, annotated);
				}
			)
			.toArray(TypeArgument<?>[]::new);

		return new TypeArgument<>(typeClass, annotations, children);
	}

	public Class<T> getType() {
		return this.type;
	}

	public List<ValuedAnnotation> getAnnotations() {
		return this.annotations;
	}

	public TypeArgument<?>[] getChildren() {
		return this.children;
	}

	public boolean hasChildren() {
		return this.children.length != 0;
	}

	@Override
	public String toString() {
		return (
			"TypeArgument{" +
			"type=" +
			this.type +
			", children=" +
			Arrays.toString(this.children) +
			'}'
		);
	}

	/**
	 * Obtains a class from an {@link AnnotatedType} if it contains a Class or ParameterizedType
	 *
	 * @param annotated an annotated type
	 * @return the {@link Class} instance of that type
	 */
	private static Class<?> obtainClass(AnnotatedType annotated) {
		Class<?> childClass;
		if ((annotated.getType() instanceof ParameterizedType)) {
			var parameterizedType = (ParameterizedType) annotated.getType();
			childClass = (Class<?>) parameterizedType.getRawType();
		} else if (annotated.getType() instanceof Class) {
			childClass = (Class<?>) annotated.getType();
		} else {
			throw new AssertionError(
				annotated.getType() + " is not a ParameterizedType nor a Class."
			);
		}
		return childClass;
	}
}
