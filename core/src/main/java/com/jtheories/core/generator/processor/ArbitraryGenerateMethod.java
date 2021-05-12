package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.Generators;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ArbitraryGenerateMethod {

  MethodSpec generatedMethod;

  /**
   * Generates an implementation for a generator method, defined in a generator interface
   *
   * @param information   the  declared generator interface information enclosed in a {@link
   *                      GeneratorInformation} object
   * @param defaultMethod the method whose implementation needs to be generated
   */
  public ArbitraryGenerateMethod(GeneratorInformation information,
      ExecutableElement defaultMethod) {

    var generatorInterface = information.getGeneratorType();
    var returnType = information.getReturnClassName();
    var generatedCode = generateCodeBlock(generatorInterface, defaultMethod);
    var methodBuilder =
        MethodSpec.methodBuilder("generate")
            .addModifiers(Modifier.PUBLIC)
            .returns(returnType)
            .addCode(generatedCode);

    this.generatedMethod = methodBuilder.build();
  }

  /**
   * Generates a code block implementing a generator method that calls its parent's default
   * implementation with a generated value for each of its parameters
   *
   * @param generatorInterface the interface annotated with {@link Generator}
   * @param defaultMethod      the generator method implemented by default on the interface
   * @return a {@link CodeBlock} containing the code for the generated implementation
   */
  private static CodeBlock generateCodeBlock(
      TypeElement generatorInterface, ExecutableElement defaultMethod) {
    var codeBlockBuilder = CodeBlock.builder();

    // Add assignment for each parameter
    defaultMethod.getParameters().stream()
        .map(ArbitraryGenerateMethod::generateAssignment)
        .forEach(codeBlockBuilder::addStatement);

    // Create code block
    var codeBlock =
        defaultMethod.getParameters().stream()
            .map(parameter -> CodeBlock.of("generated_$N", ParameterSpec.get(parameter)))
            .collect(CodeBlock.joining(", "));

    // Add return of parent's default method result
    codeBlockBuilder.addStatement(
        "return $T.super.$L($L)",
        TypeName.get(generatorInterface.asType()),
        defaultMethod.getSimpleName(),
        codeBlock);

    return codeBlockBuilder.build();
  }

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
  private static CodeBlock generateAssignment(VariableElement parameter) {
    var annotationMirrors = parameter.getAnnotationMirrors();

    var parameterType = TypeName.get(parameter.asType());
    var parameterSpec = ParameterSpec.get(parameter);
    var parentInterface = ClassName.get(Generators.class);

    if (annotationMirrors.isEmpty()) {
      return CodeBlock.of(
          "$T generated_$N = $T.gen($T.class)",
          parameterType,
          parameterSpec,
          parentInterface,
          parameterType);
    } else {
      List<CodeBlock> annotatedTypes =
          annotationMirrors.stream()
              .map(AnnotationMirror::getAnnotationType)
              .map(ClassName::get)
              .map(annotationType -> CodeBlock.of("$T.class", annotationType))
              .collect(Collectors.toList());

      return CodeBlock.of(
          "$T generated_$N = $T.gen($T.class, $L)",
          parameterType,
          parameterSpec,
          parentInterface,
          parameterType,
          CodeBlock.join(annotatedTypes, ", "));
    }
  }

  public MethodSpec getGeneratedMethod() {
    return generatedMethod;
  }
}
