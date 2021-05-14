package com.jtheories.core.generator.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArbitraryGenerateConstrainedMethod {

  private final MethodSpec constrainedMethod;
  private final ClassName returnType;

  public ArbitraryGenerateConstrainedMethod(ClassName returnType) {
    this.returnType = returnType;
    var generatedClassName = returnType.simpleName();

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
            .addParameter(Class.class, "type")
            .addParameter(Class[].class, "annotations")
            .varargs(true)
            .addStatement("$T constrained$N = generate()", returnType, generatedClassName)
            .beginControlFlow("for(Class annotation:annotations)")
            .addStatement(
                "constrained$N = ($T)this.getClass()\n"
                    + ".getDeclaredMethod(\"generate\"+annotation.getSimpleName(),$T.class)\n"
                    + ".invoke(this,constrained$N) ",
                generatedClassName,
                returnType,
                returnType,
                generatedClassName)
            .endControlFlow()
            .addStatement("return constrained$N", generatedClassName)
            .returns(returnType)
            .build();
  }

  public ClassName getReturnType() {
    return returnType;
  }

  public MethodSpec getConstrainedMethod() {
    return constrainedMethod;
  }
}
