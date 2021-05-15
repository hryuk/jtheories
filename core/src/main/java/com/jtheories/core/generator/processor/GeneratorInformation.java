package com.jtheories.core.generator.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class GeneratorInformation {

  private static final String ARBITRARY_TEMPLATE = "Arbitrary%sGenerator";
  private static final String GENERICS_TEMPLATE = "Generic%sGenerator";

  private final Types typeUtils;
  private final TypeElement generatorType;
  private final ClassName className;
  private final TypeElement returnType;
  private final ClassName returnClassName;
  private final String generatorPackage;
  private final String implementerName;

  public GeneratorInformation(Types typeUtils, TypeElement generatorType) {
    this.typeUtils = typeUtils;
    this.generatorType = generatorType;
    this.returnType = getGeneratorReturnTypeElement(generatorType);
    this.returnClassName = ClassName.get(returnType);
    this.className = ClassName.get(generatorType);
    this.generatorPackage = this.className.packageName();
    this.implementerName =
        String.format(
            isParameterized() ? GENERICS_TEMPLATE : ARBITRARY_TEMPLATE,
            this.returnClassName.simpleName());
  }

  public ClassName getReturnClassName() {
    return returnClassName;
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

  public String getImplementerName() {
    return implementerName;
  }

  public String getGeneratorPackage() {
    return generatorPackage;
  }

  public ClassName getClassName() {
    return className;
  }

  public TypeElement getGeneratorType() {
    return generatorType;
  }

  public TypeElement getReturnType() {
    return returnType;
  }

  public boolean isParameterized() {
    return !this.getReturnType().getTypeParameters().isEmpty();
  }

  public String getSimpleName(TypeMirror type) {
    return this.typeUtils.asElement(type).getSimpleName().toString();
  }
}
