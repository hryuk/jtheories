package com.jtheories.junit;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.exceptions.GenerationRuntimeException;
import com.jtheories.core.generator.exceptions.GeneratorInstantiationException;
import com.jtheories.core.generator.exceptions.IllegalGeneratorMethodAccessException;
import com.jtheories.core.generator.exceptions.MissingGeneratorMethodException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JTheoriesExtension implements ParameterResolver {

  private final List<Class<?>> availableGenerators = new ArrayList<>();
  private final List<Object> generators = new ArrayList<>();

  public JTheoriesExtension() {
    try (var scanResult = new ClassGraph().enableAllInfo().scan()) {
      ClassInfoList generatorClasses =
          scanResult.getClassesImplementing("com.jtheories.core.generator.Generator");
      for (ClassInfo annotatedClass : generatorClasses) {
        generators.add(annotatedClass.loadClass().getConstructor().newInstance());

        Method generateMethod;
        generateMethod =
            annotatedClass.loadClass().getDeclaredMethod(Generators.GENERATE, Class[].class);
        availableGenerators.add(generateMethod.getReturnType());
      }
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InstantiationException
        | InvocationTargetException e) {
      throw new GeneratorInstantiationException(
          String.format("Could not instantiate generator <%s>", e.getClass().getName()), e);
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
                        .getDeclaredMethod(Generators.GENERATE, Class[].class)
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
      generateMethod = generator.getClass().getDeclaredMethod(Generators.GENERATE, Class[].class);
    } catch (NoSuchMethodException e) {
      throw new MissingGeneratorMethodException(
          String.format(
              "Could not find generate() method on generator %s", generator.getClass().getName()),
          e);
    }

    try {
      var parameterType = parameterContext.getParameter().getParameterizedType();
      if (parameterType instanceof ParameterizedType) {
        var generatorType =
            (Class<?>) ((ParameterizedType) parameterType).getActualTypeArguments()[0];
        return generateMethod.invoke(generator, new Object[] {new Class[] {generatorType}});
      } else {
        Class<?>[] annotations =
            Arrays.stream(parameterContext.getParameter().getAnnotations())
                .map(Annotation::annotationType)
                .toArray(Class[]::new);
        return generateMethod.invoke(generator, new Object[] {annotations});
      }

    } catch (IllegalAccessException e) {
      throw new IllegalGeneratorMethodAccessException(
          String.format(
              "Could not access generate() method on generator %s", generator.getClass().getName()),
          e);
    } catch (InvocationTargetException e) {
      throw new GenerationRuntimeException(
          String.format(
              "Method generate() on class %s threw an exception", generator.getClass().getName()),
          e);
    }
  }
}
