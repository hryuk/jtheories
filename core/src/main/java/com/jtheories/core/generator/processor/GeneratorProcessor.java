package com.jtheories.core.generator.processor;

import com.google.auto.service.AutoService;
import com.jtheories.core.generator.Generator;
import com.squareup.javapoet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.jtheories.core.generator.processor.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractProcessor {

  private Messager messager = null;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
  }

  void fatal(String format, Object... args) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
    throw new RuntimeException();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
      annotatedElements.forEach(
          element -> {
            if (element.getKind() != ElementKind.INTERFACE) {
              fatal(
                  "Illegal use of @Generator on non-interface element %s", element.getSimpleName());
            }
          });

      Collection<TypeElement> annotatedInterfaces =
          annotatedElements.stream().map(TypeElement.class::cast).collect(Collectors.toList());

      for (TypeElement annotatedInterface : annotatedInterfaces) {
        ExecutableElement defaultMethod =
            annotatedInterface.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .findAny()
                .orElseThrow();

        if (defaultMethod.getParameters().size() != 1) {
          fatal(
              "Default generate() definition receiving more than one parameter in %s",
              annotatedInterface.getSimpleName());
        }

        AnnotationSpec generatedAnnotaion =
            AnnotationSpec.builder(Generated.class).addMember("value", "$S", "JTheories").build();

        CodeBlock codeBlock =
            CodeBlock.builder()
                .addStatement(
                    "return $T.super.generate($N)",
                    ClassName.get(annotatedInterface.asType()),
                    ParameterSpec.get(defaultMethod.getParameters().get(0)))
                .build();

        MethodSpec method = MethodSpec.overriding(defaultMethod).addCode(codeBlock).build();

        String annotatedElementClassName = TypeName.get(annotatedInterface.asType()).toString();
        String generatedClassName =
            TypeName.get(getGenerateReturnType(annotatedInterface)).toString();
        String generatorPackage =
            annotatedElementClassName.substring(0, annotatedElementClassName.lastIndexOf('.'));
        String generatorClassName =
            String.format(
                "Arbitrary%sGenerator",
                generatedClassName.substring(generatedClassName.lastIndexOf('.') + 1));

        TypeSpec arbitraryObject =
            TypeSpec.classBuilder(generatorClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(
                    ParameterizedTypeName.get(
                        ClassName.get(Generator.class),
                        ClassName.get(getGenerateReturnType(annotatedInterface))))
                .addSuperinterface(ClassName.get(annotatedInterface.asType()))
                .addAnnotation(generatedAnnotaion)
                .addMethod(method)
                .build();

        JavaFile javaFile = JavaFile.builder(generatorPackage, arbitraryObject).build();

        JavaFileObject builderFile;
        String sourceFileName = generatorPackage + "." + generatorClassName;

        try {
          builderFile = processingEnv.getFiler().createSourceFile(sourceFileName);
        } catch (IOException e) {
          fatal("Error creating generated file %s", sourceFileName);
          return true;
        }
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
          javaFile.writeTo(out);
        } catch (IOException e) {
          fatal("Error writing generated file %s", sourceFileName);
        }
      }
    }

    return true;
  }

  private TypeMirror getGenerateReturnType(TypeElement element) {
    return element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .map(Element::asType)
        .map(ExecutableType.class::cast)
        .map(ExecutableType::getReturnType)
        .findFirst()
        .orElseThrow();
  }
}
