package com.jtheories.core.generator.processor.generic;

import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

public class GenericGenerateConstrainedMethod {

  private final MethodSpec constrainedMethod;
  private final GeneratorInformation information;

  public GenericGenerateConstrainedMethod(GeneratorInformation information) {
    this.information = information;

    this.constrainedMethod =
        MethodSpec.methodBuilder("generateConstrained")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Class.class, "type")
            .addParameter(Class[].class, "annotations")
            .varargs(true)
            .addStatement(
                "return $T.super.generate(type, annotations)", this.information.getClassName())
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
