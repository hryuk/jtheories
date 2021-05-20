package com.jtheories.junit;

import com.jtheories.core.generator.Generators;
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
		try {
			var typeArgument = Generators.getTypeArgument(
				parameterContext.getParameter().getParameterizedType(),
				parameterContext.getParameter().getAnnotatedType()
			);
			var generator = Generators.getGenerator(typeArgument.getType());
			return generator.generate(typeArgument);
		} catch (Exception e) {
			// e.printStackTrace();
			throw e;
		}
	}
}
