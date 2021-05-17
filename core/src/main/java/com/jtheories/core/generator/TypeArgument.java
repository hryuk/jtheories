package com.jtheories.core.generator;

public class TypeArgument {

	private Class type;
	private Class[] annotations;

	public TypeArgument(Class type, Class[] annotations) {
		this.type = type;
		this.annotations = annotations;
	}

	public Class getType() {
		return this.type;
	}

	public Class[] getAnnotations() {
		return this.annotations;
	}
}
