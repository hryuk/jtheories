package com.jtheories.core.generator;

import com.jtheories.core.generator.exceptions.GeneratorInstantiationException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Generators {

	/**
	 * Expected name of the generator method
	 */
	public static final String GENERATE = "generate";

	private static final ConcurrentMap<Class<?>, Generator<?>> cache = new ConcurrentHashMap<>();

	private Generators() {
		throw new AssertionError("This class cannot be instanced");
	}

	private static <T> Generator<T> createGenerator(final Class<T> generatedType) {
		try (final var scanResult = new ClassGraph().enableClassInfo().scan()) {
			final ClassInfoList arbitraryGenerators = scanResult.getClassesImplementing(
				"com.jtheories.core.generator.Generator"
			);

			for (final ClassInfo arbitraryGenerator : arbitraryGenerators) {
				final var generateMethod = arbitraryGenerator
					.loadClass()
					.getDeclaredMethod(Generators.GENERATE, List.class);
				if (generateMethod.getReturnType().equals(generatedType)) {
					//noinspection unchecked
					return (Generator<T>) arbitraryGenerator
						.loadClass()
						.getConstructor()
						.newInstance();
				}
			}

			throw new GeneratorInstantiationException(
				String.format("Could not find generator for %s", generatedType.getName())
			);
		} catch (
			final NoSuchMethodException
			| IllegalAccessException
			| InstantiationException
			| InvocationTargetException e
		) {
			throw new GeneratorInstantiationException(
				String.format("Could not instantiate generator <%s>", e.getClass().getName())
			);
		}
	}

	public static <T> Generator<T> getGenerator(final Class<T> generatedType) {
		//noinspection unchecked
		return (Generator<T>) cache.computeIfAbsent(
			generatedType,
			Generators::createGenerator
		);
	}

	public static <T> T gen(final Class<T> generatedType, final Class<?>... annotations) {
		final Generator<T> generator = getGenerator(generatedType);
		return generator.generate(
			Arrays.asList(new TypeArgument(generatedType, annotations))
		);
	}
}
