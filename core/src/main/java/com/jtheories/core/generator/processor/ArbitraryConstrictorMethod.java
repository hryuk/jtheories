package com.jtheories.core.generator.processor;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public class ArbitraryConstrictorMethod {

  private final MethodSpec generatedMethod;

  public ArbitraryConstrictorMethod(
      GeneratorInformation information,
      Element annotation,
      ExecutableElement defaultMethod) {

    var generatedClassSimpleName = information.getReturnClassName().simpleName();

    var generatedCode =
        CodeBlock.builder()
            .addStatement(
                "return $T.super.$L($L)",
                TypeName.get(information.getGeneratorType().asType()),
                defaultMethod.getSimpleName(),
                String.format("arbitrary%s", generatedClassSimpleName))
            .build();

    var name = "generate" + annotation.getSimpleName();

    var methodBuilder =
        MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .returns(information.getReturnClassName())
            .addCode(generatedCode);

    methodBuilder.addParameter(information.getReturnClassName(),
        String.format("arbitrary%s", generatedClassSimpleName));

    this.generatedMethod = methodBuilder.build();
  }

  public MethodSpec getGeneratedMethod() {
    return generatedMethod;
  }
}
