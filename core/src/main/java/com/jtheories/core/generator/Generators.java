package com.jtheories.core.generator;

import com.jtheories.core.generator.exceptions.GeneratorInstantiationException;
import com.jtheories.core.generator.exceptions.NoSuchGeneratorException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

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

	public static <T> T gen(TypeArgument<T> typeArgument) {
		return getGenerator(typeArgument.getType()).generate(typeArgument);
	}

	public static TypeArgument<?> getTypeArgument(Type type, AnnotatedType annotatedType) {
		var name = Optional
			.of(type)
			.filter(ParameterizedType.class::isInstance)
			.map(ParameterizedType.class::cast)
			.map(ParameterizedType::getRawType)
			.orElse(annotatedType.getType())
			.getTypeName();

		Class<?> clazz;
		try {
			clazz = Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new GeneratorInstantiationException(
				String.format("Unable to find class %s", name),
				e
			);
		}

		Annotation[] annotations = annotatedType.getDeclaredAnnotations();

		TypeArgument<?>[] children = Stream
			.of(annotatedType)
			.filter(AnnotatedParameterizedType.class::isInstance)
			.map(AnnotatedParameterizedType.class::cast)
			.map(AnnotatedParameterizedType::getAnnotatedActualTypeArguments)
			.flatMap(Arrays::stream)
			.map(annotated -> Generators.getTypeArgument(annotated.getType(), annotated))
			.toArray(TypeArgument[]::new);

		return new TypeArgument<>(clazz, annotations, children);
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
				String.format("Could not instantiate generator <%s>", method.getDeclaringClass()),
				e
			);
		}
	}

	private static Method getGenerateMethod(ClassInfo implementation) {
		try {
			return implementation
				.loadClass()
				.getDeclaredMethod(Generators.GENERATE, TypeArgument.class);
		} catch (NoSuchMethodException e) {
			throw new GeneratorInstantiationException(
				String.format("Could not instantiate generator <%s>", implementation.getName()),
				e
			);
		}
	}
}
