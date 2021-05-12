package com.jtheories.core.generator.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ArbitraryConstrictorMethod {

  private MethodSpec generatedMethod;

  public ArbitraryConstrictorMethod(
      TypeElement generatorInterface,
      TypeName annotation,
      ClassName returnType,
      ExecutableElement defaultMethod) {

    var generatedClassSimpleName =
        returnType.toString().substring(returnType.toString().lastIndexOf('.') + 1);

    var generatedCode =
        CodeBlock.builder()
            .addStatement(
                "return $T.super.$L($L)",
                TypeName.get(generatorInterface.asType()),
                defaultMethod.getSimpleName(),
                String.format("arbitrary%s", generatedClassSimpleName))
            .build();

    var name =
        "generate" + annotation.toString().substring(annotation.toString().lastIndexOf('.') + 1);

    var methodBuilder =
        MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .returns(returnType)
            .addCode(generatedCode);

    methodBuilder.addParameter(returnType, String.format("arbitrary%s", generatedClassSimpleName));

    this.generatedMethod = methodBuilder.build();
  }

  public MethodSpec getGeneratedMethod() {
    return generatedMethod;
  }
}
