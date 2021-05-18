package com.jtheories.core.generator;

import com.jtheories.core.generator.exceptions.GeneratorInstantiationException;
import com.jtheories.core.generator.exceptions.NoSuchGeneratorException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

	/**
	 * Return true if there's a generator available for the given type
	 *
	 * @param generatedType the {@link Class} of the type to check
	 * @param <T>           the type to check
	 * @return true if a generator can be obtained for the given type, false otherwise
	 */
	public static <T> boolean hasGenerator(final Class<T> generatedType) {
		try {
			getGenerator(generatedType);
			return true;
		} catch (NoSuchGeneratorException e) {
			return false;
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
			Collections.singletonList(new TypeArgument(generatedType, annotations))
		);
	}

	private static <T> Generator<T> createGenerator(final Class<T> generatedType) {
		Optional<Method> generatorMethod = getGeneratorMethod(generatedType);
		return generatorMethod
			.map(Generators::<T>instantiateGenerator)
			.orElseThrow(
				() ->
					new NoSuchGeneratorException(
						String.format("Could not find generator for %s", generatedType.getName())
					)
			);
	}

	private static <T> Optional<Method> getGeneratorMethod(Class<T> generatedType) {
		Optional<Method> generatorMethod;
		try (final var scanResult = new ClassGraph().enableClassInfo().scan()) {
			final ClassInfoList generatorImplementations = scanResult.getClassesImplementing(
				"com.jtheories.core.generator.Generator"
			);

			generatorMethod =
				generatorImplementations
					.stream()
					.map(Generators::getGenerateMethod)
					.filter(method -> method.getReturnType().equals(generatedType))
					// TODO: We should be careful with this... what if there's more than one?
					//  Throw an exception? Define criteria for picking?
					.findAny();
		}
		return generatorMethod;
	}

	private static <T> Generator<T> instantiateGenerator(Method method) {
		try {
			//noinspection unchecked
			return (Generator<T>) method.getDeclaringClass().getConstructor().newInstance();
		} catch (
			InstantiationException
			| IllegalAccessException
			| InvocationTargetException
			| NoSuchMethodException e
		) {
			throw new GeneratorInstantiationException(
				String.format("Could not instantiate generator <%s>", e.getClass().getName())
			);
		}
	}

	private static Method getGenerateMethod(ClassInfo implementation) {
		try {
			return implementation
				.loadClass()
				.getDeclaredMethod(Generators.GENERATE, List.class);
		} catch (NoSuchMethodException e) {
			throw new GeneratorInstantiationException(
				String.format("Could not instantiate generator <%s>", e.getClass().getName())
			);
		}
	}
}
