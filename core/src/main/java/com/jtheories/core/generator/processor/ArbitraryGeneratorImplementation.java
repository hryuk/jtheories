package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.Generator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.Generated;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ArbitraryGeneratorImplementation {

  private final JavaFile javaFile;
  private final String fileName;
  private final GeneratorInformation information;

  /**
   * Given a {@link TypeElement} representing a generator interface, generate its implementation
   *
   * @param information the information about the interface to be implemented contained in a {@link
   *                    GeneratorInformation} object
   */
  public ArbitraryGeneratorImplementation(GeneratorInformation information) {
    this.information = information;

    AnnotationSpec generatedAnnotation =
        AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", GeneratorProcessor.class.getName())
            .build();

    List<MethodSpec> generatorMethods = new ArrayList<>();
    MethodSpec generate = this.information
        .getGeneratorType()
        .getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .filter(e -> e.getAnnotationMirrors().isEmpty())
        .map(ExecutableElement.class::cast)
        .map(
            executableElement ->
                new ArbitraryGenerateMethod(
                    this.information.getGeneratorType(),
                    this.information.getReturnClassName(),
                    executableElement))
        .map(ArbitraryGenerateMethod::getGeneratedMethod)
        .findAny()
        .orElseThrow();

    generatorMethods.add(generate);

    List<MethodSpec> constrictorMethods = this.information
        .getGeneratorType()
        .getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .filter(e -> e.getAnnotationMirrors().size() == 1)
        .map(ExecutableElement.class::cast)
        .map(this::createArbitraryConstrictorMethod)
        .map(ArbitraryConstrictorMethod::getGeneratedMethod)
        .collect(Collectors.toList());

    generatorMethods.addAll(constrictorMethods);

    generatorMethods.add(
        new ArbitraryGenerateConstrainedMethod(this.information.getReturnClassName())
            .getConstrainedMethod());

    TypeSpec arbitraryGenerator =
        TypeSpec.classBuilder(this.information.getImplementerName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(this.information.getClassName())
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(Generator.class),
                    this.information.getReturnClassName()))
            .addAnnotation(generatedAnnotation)
            .addMethods(generatorMethods)
            .build();

    this.javaFile = JavaFile.builder(
        this.information.getGeneratorPackage(),
        arbitraryGenerator)
        .build();
    this.fileName =
        this.information.getGeneratorPackage() + "." + this.information.getImplementerName();
  }

  private ArbitraryConstrictorMethod createArbitraryConstrictorMethod(
      ExecutableElement executableElement) {
    var constrictorAnnotation = executableElement.getAnnotationMirrors()
        .get(0)
        .getAnnotationType()
        .asElement();
    return new ArbitraryConstrictorMethod(
        this.information,
        constrictorAnnotation,
        executableElement);
  }

  public boolean isParameterized() {
    return !this.information
        .getReturnType()
        .getTypeParameters()
        .isEmpty();
  }

  public JavaFile getJavaFile() {
    return javaFile;
  }

  public String getFileName() {
    return fileName;
  }
}
