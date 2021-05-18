package com.jtheories.junit;

import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.TypeArgument;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class JTheoriesExtension implements ParameterResolver {

	@Override
	public boolean supportsParameter(
		ParameterContext parameterContext,
		ExtensionContext extensionContext
	) throws ParameterResolutionException {
		return Generators.hasGenerator(parameterContext.getParameter().getType());
	}

	@Override
	public Object resolveParameter(
		ParameterContext parameterContext,
		ExtensionContext extensionContext
	) throws ParameterResolutionException {
		Generator<?> generator = Generators.getGenerator(
			parameterContext.getParameter().getType()
		);

		List<TypeArgument> typeArguments =
			this.getTypeArguments(parameterContext.getParameter());
		return generator.generate(typeArguments);
	}

	private List<TypeArgument> getTypeArguments(Parameter parameter) {
		final List<TypeArgument> typeArguments = new ArrayList<>();

		if (parameter.getParameterizedType() instanceof ParameterizedType) {
			Arrays
				.stream(
					(
						(AnnotatedParameterizedType) parameter.getAnnotatedType()
					).getAnnotatedActualTypeArguments()
				)
				.forEach(
					argumentType ->
						typeArguments.add(
							new TypeArgument(
								(Class<?>) argumentType.getType(),
								Arrays
									.stream(argumentType.getAnnotations())
									.map(Annotation::annotationType)
									.toArray(Class<?>[]::new)
							)
						)
				);
		} else {
			Class<?>[] annotations = Arrays
				.stream(parameter.getAnnotations())
				.map(Annotation::annotationType)
				.toArray(Class[]::new);
			typeArguments.add(new TypeArgument(null, annotations));
		}

		return typeArguments;
	}
}
