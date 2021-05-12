package com.jtheories.core.generator.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArbitraryConstrainedMethod {

  private final MethodSpec constrainedMethod;
  private final ClassName returnType;

  /**
   * Generates a random value assignment {@link CodeBlock} for a parameter. The generated code has
   * looks like this:
   *
   * <p>{@code Parameter generated_parameter = Generators.gen(Parameter.class)}<br>
   *
   * <p>If the parameter is annotated with a constraint, it will be taken into account, generating
   * code like this instead:
   *
   * <p>{@code Parameter generated_parameter = Generators.gen(Parameter.class,
   * ...[Constraint.class])}
   *
   * @param parameter a generator method's parameter represented by a {@link VariableElement}
   * @return a {@link CodeBlock} with the assignment code
   */
  public ArbitraryConstrainedMethod(ClassName returnType) {
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

  public MethodSpec getConstrainedMethod() {
    return constrainedMethod;
  }
}
