package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.processor.generics.ArbitraryGenericGenerateConstrainedMethod;
import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArbitraryGeneratorImplementation {

  private final JavaFile javaFile;
  private final String fileName;
  private final GeneratorInformation information;

  /**
   * Given a {@link TypeElement} representing a generator interface, generate its implementation
   *
   * @param information the information about the interface to be implemented contained in a {@link
   *     GeneratorInformation} object
   */
  public ArbitraryGeneratorImplementation(GeneratorInformation information) {
    this.information = information;
    this.fileName =
        String.format(
            "%s.%s", this.information.getGeneratorPackage(), this.information.getImplementerName());
    this.javaFile =
        JavaFile.builder(this.information.getGeneratorPackage(), createArbitraryGenerator())
            .build();
  }

  private TypeSpec createArbitraryGenerator() {
    AnnotationSpec generatedAnnotation =
        AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", GeneratorProcessor.class.getName())
            .build();

    List<MethodSpec> generatorMethods = new ArrayList<>();
    MethodSpec generate =
        this.information.getGeneratorType().getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .filter(e -> e.getAnnotationMirrors().isEmpty())
            .map(ExecutableElement.class::cast)
            .map(this::createArbitraryGenerateMethod)
            .map(ArbitraryGenerateMethod::getGeneratedMethod)
            .findAny()
            .orElseThrow();

    generatorMethods.add(generate);

    List<MethodSpec> constrictorMethods =
        this.information.getGeneratorType().getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .filter(e -> e.getAnnotationMirrors().size() == 1)
            .map(ExecutableElement.class::cast)
            .map(this::createArbitraryConstrictorMethod)
            .map(ArbitraryConstrictorMethod::getGeneratedMethod)
            .collect(Collectors.toList());

    generatorMethods.addAll(constrictorMethods);

    if (this.information.isParameterized()) {
      generatorMethods.add(
          new ArbitraryGenericGenerateConstrainedMethod(this.information).getConstrainedMethod());
    } else {
      generatorMethods.add(
          new ArbitraryGenerateConstrainedMethod(this.information.getReturnClassName())
              .getConstrainedMethod());
    }

    TypeSpec.Builder typeBuilder =
        TypeSpec.classBuilder(this.information.getImplementerName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(this.information.getClassName())
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(Generator.class), this.information.getReturnClassName()))
            .addAnnotation(generatedAnnotation)
            .addMethods(generatorMethods);

    if (information.isParameterized()) {
      typeBuilder.addTypeVariable(TypeVariableName.get("T"));
    }

    return typeBuilder.build();
  }

  private ArbitraryGenerateMethod createArbitraryGenerateMethod(
      ExecutableElement executableElement) {
    return new ArbitraryGenerateMethod(this.information, executableElement);
  }

  private ArbitraryConstrictorMethod createArbitraryConstrictorMethod(
      ExecutableElement executableElement) {
    var constrictorAnnotation =
        executableElement.getAnnotationMirrors().get(0).getAnnotationType().asElement();
    return new ArbitraryConstrictorMethod(
        this.information, constrictorAnnotation, executableElement);
  }

  public JavaFile getJavaFile() {
    return javaFile;
  }

  public String getFileName() {
    return fileName;
  }
}
