package com.jtheories.generators.processor;

import com.google.auto.service.AutoService;
import com.jtheories.generators.Generator;
import com.jtheories.random.SourceOfRandom;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(
        "com.jtheories.generators.processor.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            TypeElement annotatedElement =(TypeElement) roundEnv.getElementsAnnotatedWith(annotation).stream().findFirst().orElseThrow();
            ExecutableElement defaultMethod = annotatedElement.getEnclosedElements()
                    .stream()
                    .filter(e -> e.getKind()== ElementKind.METHOD)
                    .map(ExecutableElement.class::cast)
                    .findAny().orElseThrow();

            AnnotationSpec generatedAnnotaion = AnnotationSpec.builder(Generated.class)
                    .addMember("value","$S","JTheories")
                    .build();

            CodeBlock codeBlock = CodeBlock.builder()
                    .addStatement("return $T.super.generate(random)", ClassName.get(annotatedElement.asType()))
                    .build();

            MethodSpec method = MethodSpec.overriding(defaultMethod)
                    .addCode(codeBlock)
                    .build();

            TypeSpec arbitraryProduct = TypeSpec.classBuilder("ArbitraryProduct")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Generator.class), ClassName.get(getGenerateReturnType(annotatedElement))))
                    .addSuperinterface(ClassName.get(annotatedElement.asType()))
                    .addAnnotation(generatedAnnotaion)
                    .addMethod(method)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.jtheories.examples.ArbitraryProduct", arbitraryProduct)
                    .build();

            JavaFileObject builderFile = null;
            try {
                builderFile = processingEnv.getFiler()
                        .createSourceFile("com.jtheories.examples.ArbitraryProduct");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                javaFile.writeTo(out);
            } catch (IOException e) {
                throw new RuntimeException("Error writing generated file ");
            }
        }

        return true;
    }

    private TypeMirror getGenerateReturnType(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind()== ElementKind.METHOD)
                .map(Element::asType)
                .map(ExecutableType.class::cast)
                .map(ExecutableType::getReturnType)
                .findFirst()
                .orElseThrow();
    }

}
