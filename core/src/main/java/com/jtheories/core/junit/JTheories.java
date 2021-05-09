package com.jtheories.core.junit;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.random.SourceOfRandom;
import io.github.classgraph.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.*;

public class JTheories implements ParameterResolver {

  private final List<Class<?>> availableGenerators = new ArrayList<>();
  private final List<Object> generators = new ArrayList<>();

  public JTheories() {
    try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().scan()) {
      ClassInfoList annotatedClasses =
          scanResult.getClassesImplementing("com.jtheories.core.generator.Generator");
      for (ClassInfo annotatedClass : annotatedClasses) {
        generators.add(annotatedClass.loadClass().getConstructor().newInstance());
        Method generateMethod =
            annotatedClass.loadClass().getDeclaredMethod("generate", SourceOfRandom.class);
        availableGenerators.add(generateMethod.getReturnType());
      }
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InstantiationException
        | InvocationTargetException e) {
      throw new RuntimeException(
          String.format("Could not instantiate generator <%s>", e.getClass().getName()));
    }
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return availableGenerators.stream()
        .anyMatch(generatorType -> parameterContext.getParameter().getType().equals(generatorType));
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Object generator =
        generators.stream()
            .filter(
                gen -> {
                  try {
                    return gen.getClass()
                        .getDeclaredMethod("generate", SourceOfRandom.class)
                        .getReturnType()
                        .equals(parameterContext.getParameter().getType());
                  } catch (NoSuchMethodException e) {
                    return false;
                  }
                })
            .findAny()
            .orElseThrow();

    Method generateMethod;
    try {
      generateMethod = generator.getClass().getDeclaredMethod("generate", SourceOfRandom.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(
          String.format(
              "Could not find generate() method on generator %s", generator.getClass().getName()));
    }

    try {
      return generateMethod.invoke(generator, Generators.SOURCE_OF_RANDOM);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          String.format(
              "Could not access generate() method on generator %s",
              generator.getClass().getName()));
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      throw new RuntimeException(
          String.format(
              "Could not call generate() method on generator %s", generator.getClass().getName()));
    }
  }
}
