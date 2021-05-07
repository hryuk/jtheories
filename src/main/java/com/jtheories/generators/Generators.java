package com.jtheories.generators;

import com.jtheories.random.SourceOfRandom;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Generators {

    public static final SourceOfRandom SOURCE_OF_RANDOM = new SourceOfRandom();

    public static <T> Generator<T> getGenerator(Class<T> generatedType){
        try (ScanResult scanResult =
                     new ClassGraph()
                             .enableAnnotationInfo()
                             .scan()) {
            ClassInfoList annotatedClasses = scanResult.getClassesImplementing("com.jtheories.generators.Generator");
            for (ClassInfo annotatedClass : annotatedClasses) {
                Method generateMethod = annotatedClass.loadClass().getDeclaredMethod("generate", SourceOfRandom.class);
                if(generateMethod.getReturnType().equals(generatedType)) {
                    return (Generator<T>)annotatedClass.loadClass().getConstructor().newInstance();
                }
            }

            throw new RuntimeException(String.format("Could not find generator for %s",generatedType.getClass().getName()));
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Could not instantiate generator <%s>",e.getClass().getName()));
        }
    }

    public static <T> T gen(Class<T> generatedType, SourceOfRandom random){
        Generator<T> generator =getGenerator(generatedType);
        return generator.generate(random);
    }
}
