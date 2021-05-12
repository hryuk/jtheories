package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.Generator;
import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArbitraryGeneratorImplementation {
  private final TypeElement generatorInterface;
  private final ClassName generatorReturnType;
  private final JavaFile javaFile;
  private final String fileName;
  private final Types typeUtils;

  /**
   * Given a {@link TypeElement} representing a generator interface, generate its implementation
   *
   * @param generatorInterface the interface to implement
   * @param typeUtils
   */
  public ArbitraryGeneratorImplementation(TypeElement generatorInterface, Types typeUtils) {
    this.generatorInterface = generatorInterface;
    this.typeUtils = typeUtils;

    var annotatedElementClassName = ClassName.get(generatorInterface);
    this.generatorReturnType = getGeneratorReturnType(generatorInterface);
    var generatorPackage = annotatedElementClassName.packageName();
    var generatorClassName =
        String.format("Arbitrary%sGenerator", generatorReturnType.simpleName());

    AnnotationSpec generatedAnnotation =
        AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", GeneratorProcessor.class.getName())
            .build();

    List<MethodSpec> generatorMethods = new ArrayList<>();
    MethodSpec generate =
        generatorInterface.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .filter(e -> e.getAnnotationMirrors().isEmpty())
            .map(ExecutableElement.class::cast)
            .map(
                executableElement ->
                    new ArbitraryGenerateMethod(
                        generatorInterface, generatorReturnType, executableElement))
            .map(ArbitraryGenerateMethod::getGeneratedMethod)
            .findAny()
            .orElseThrow();

    generatorMethods.add(generate);

    List<MethodSpec> constrictorMethods =
        generatorInterface.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .filter(e -> e.getAnnotationMirrors().size() == 1)
            .map(ExecutableElement.class::cast)
            .map(
                executableElement ->
                    new ArbitraryConstrictorMethod(
                        generatorInterface,
                        TypeName.get(
                            executableElement.getAnnotationMirrors().get(0).getAnnotationType()),
                        generatorReturnType,
                        executableElement))
            .map(ArbitraryConstrictorMethod::getGeneratedMethod)
            .collect(Collectors.toList());

    generatorMethods.addAll(constrictorMethods);

    generatorMethods.add(
        new ArbitraryGenerateConstrainedMethod(generatorReturnType).getConstrainedMethod());

    TypeSpec arbitraryGenerator =
        TypeSpec.classBuilder(generatorClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(generatorInterface.asType()))
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(Generator.class), generatorReturnType))
            .addAnnotation(generatedAnnotation)
            .addMethods(generatorMethods)
            .build();

    this.javaFile = JavaFile.builder(generatorPackage, arbitraryGenerator).build();
    this.fileName = generatorPackage + "." + generatorClassName;
  }

  /**
   * Given a generator class represented by a {@link TypeElement}, obtain the return type of the
   * generator
   *
   * @param element the class of the generator
   * @return the return type of the generator as a {@link TypeMirror}
   */
  private TypeElement getGeneratorReturnTypeElement(TypeElement element) {
    // TODO: This only works if the generator has only one return type among its methods
    //  (or only has one method), it will return any of its return types otherwise, we need to
    //  make sure that generators only have one return type or else make necessary arrangements to
    //  support more of them
    return element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .map(Element::asType)
        .map(ExecutableType.class::cast)
        .map(ExecutableType::getReturnType)
        .map(this.typeUtils::asElement)
        .filter(TypeElement.class::isInstance)
        .map(TypeElement.class::cast)
        .findFirst()
        .orElseThrow();
  }

  public ClassName getGeneratorReturnType(TypeElement element) {
    return ClassName.get(getGeneratorReturnTypeElement(element));
  }

  public boolean isParameterized(TypeElement element) {
    var typeElement = getGeneratorReturnTypeElement(element);
    return !typeElement.getTypeParameters().isEmpty();
  }

  public JavaFile getJavaFile() {
    return javaFile;
  }

  public String getFileName() {
    return fileName;
  }
}
