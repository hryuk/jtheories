package com.jtheories.core.runner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class JTheories {

	private JTheories() {}

	@SuppressWarnings("unchecked")
	public static <T> Theory<T> forAll() {
		var stackTraceElement = Thread.currentThread().getStackTrace()[2];
		var theoryClassName = String.format(
			"Theory_%s_L%d",
			stackTraceElement
				.getClassName()
				.substring(stackTraceElement.getClassName().lastIndexOf('.') + 1),
			stackTraceElement.getLineNumber()
		);

		var theoryPackageName = stackTraceElement
			.getClassName()
			.substring(0, stackTraceElement.getClassName().lastIndexOf('.'));

		var className = String.format("%s.%s", theoryPackageName, theoryClassName);
		Class<?> clazz;
		Object theory;
		try {
			clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor();
			theory = ctor.newInstance();
		} catch (
			ClassNotFoundException
			| NoSuchMethodException
			| InstantiationException
			| IllegalAccessException
			| InvocationTargetException e
		) {
			e.printStackTrace();
			throw new RuntimeException("Error instancing requested theory", e);
		}

		return (Theory<T>) theory;
	}
}
