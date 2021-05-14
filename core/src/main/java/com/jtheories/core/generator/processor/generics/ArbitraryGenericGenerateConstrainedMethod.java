package com.jtheories.core.generator.processor.generics;

import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArbitraryGenericGenerateConstrainedMethod {

  private final MethodSpec constrainedMethod;
  private final GeneratorInformation information;

  public ArbitraryGenericGenerateConstrainedMethod(GeneratorInformation information) {
    this.information = information;
    var generatedClassName = information.getReturnClassName().simpleName();

    this.constrainedMethod =
        MethodSpec.methodBuilder("generateConstrained")
            .addModifiers(Modifier.PUBLIC)
            .addExceptions(
                Stream.of(
                        NoSuchMethodException.class,
                        InvocationTargetException.class,
                        IllegalAccessException.class)
                    .map(TypeName::get)
                    .collect(Collectors.toList()))
            .addParameter(Class[].class, "annotations")
            .varargs(true)
            // TODO: DONT use annotations parameters as generic type parameter
            .addStatement(
                "return $T.super.generate(annotations[0])", this.information.getClassName())
            .returns(this.information.getReturnClassName())
            .build();
  }

  public ClassName getReturnType() {
    return information.getReturnClassName();
  }

  public MethodSpec getConstrainedMethod() {
    return constrainedMethod;
  }
}
