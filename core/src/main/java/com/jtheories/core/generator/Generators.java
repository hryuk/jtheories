package com.jtheories.core.generator;

import com.jtheories.core.generator.exceptions.GenerationRuntimeException;
import com.jtheories.core.generator.exceptions.GeneratorInstantiationException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Generators {

  /** Expected name of the generator method */
  public static final String GENERATE = "generateConstrained";

  private Generators() {
    throw new AssertionError("This class cannot be instanced");
  }

  public static <T> Generator<T> getGenerator(Class<T> generatedType) {
    try (var scanResult = new ClassGraph().enableClassInfo().scan()) {
      ClassInfoList arbitraryGenerators =
          scanResult.getClassesImplementing("com.jtheories.core.generator.Generator");

      for (ClassInfo arbitraryGenerator : arbitraryGenerators) {
        var generateMethod =
            arbitraryGenerator
                .loadClass()
                .getDeclaredMethod(Generators.GENERATE, Class.class, Class[].class);
        if (generateMethod.getReturnType().equals(generatedType)) {
          //noinspection unchecked
          return (Generator<T>) arbitraryGenerator.loadClass().getConstructor().newInstance();
        }
      }

      throw new GeneratorInstantiationException(
          String.format("Could not find generator for %s", generatedType.getName()));
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InstantiationException
        | InvocationTargetException e) {
      throw new GeneratorInstantiationException(
          String.format("Could not instantiate generator <%s>", e.getClass().getName()));
    }
  }

  public static <T> T gen(Class<T> generatedType, Class<?>... annotations) {
    Generator<T> generator = getGenerator(generatedType);
    try {
      return generator.generateConstrained(generatedType, annotations);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new GenerationRuntimeException(
          String.format(
              "Could not call <%s>() on generator %s",
              Generators.GENERATE + Arrays.toString(annotations), generatedType.getSimpleName()),
          e);
    }
  }
}
