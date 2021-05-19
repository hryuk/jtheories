package com.jtheories.core.generator;

import java.lang.annotation.Annotation;

public class TypeArgument<T> {

	private final Class<T> type;
	private final Annotation[] annotations;
	private final TypeArgument<?>[] children;

	public TypeArgument(Class<T> type) {
		this.type = type;
		this.annotations = new Annotation[0];
		this.children = new TypeArgument[0];
	}

	public TypeArgument(Class<T> type, Annotation[] annotations) {
		this.type = type;
		this.annotations = annotations;
		this.children = new TypeArgument[0];
	}

	public TypeArgument(Class<T> type, TypeArgument<?>[] children) {
		this.type = type;
		this.annotations = new Annotation[0];
		this.children = children;
	}

	public TypeArgument(
		Class<T> type,
		Annotation[] annotations,
		TypeArgument<?>[] children
	) {
		this.type = type;
		this.annotations = annotations;
		this.children = children;
	}

	public Class<T> getType() {
		return this.type;
	}

	public Annotation[] getAnnotations() {
		return this.annotations;
	}

	public TypeArgument<?>[] getChildren() {
		return this.children;
	}

	public boolean hasChildren() {
		return this.children.length != 0;
	}
}
