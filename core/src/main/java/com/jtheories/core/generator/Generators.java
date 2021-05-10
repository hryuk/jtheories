package com.jtheories.core.generator;

import com.jtheories.core.random.SourceOfRandom;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Generators {
  public static <T> Generator<T> getGenerator(Class<T> generatedType) {
    try (ScanResult scanResult = new ClassGraph().enableClassInfo().scan()) {
      ClassInfoList arbitraryGenerators =
          scanResult.getClassesImplementing("com.jtheories.core.generator.Generator");

      for (ClassInfo arbitraryGenerator : arbitraryGenerators) {
        Method generateMethod = arbitraryGenerator.loadClass().getDeclaredMethod("generate");
        if (generateMethod.getReturnType().equals(generatedType)) {
          //noinspection unchecked
          return (Generator<T>) arbitraryGenerator.loadClass().getConstructor().newInstance();
        }
      }

      throw new RuntimeException(
          String.format("Could not find generator for %s", generatedType.getName()));
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InstantiationException
        | InvocationTargetException e) {
      throw new RuntimeException(
          String.format("Could not instantiate generator <%s>", e.getClass().getName()));
    }
  }

  public static <T> T gen(Class<T> generatedType, Class<?>... annotations) {
    Generator<T> generator = getGenerator(generatedType);
    try {
      return generator.generateConstrained(annotations);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(
          String.format(
              "Could not call <%s> on generator %s",
              "generate" + Arrays.toString(annotations), generatedType.getSimpleName()));
    }
  }
}
