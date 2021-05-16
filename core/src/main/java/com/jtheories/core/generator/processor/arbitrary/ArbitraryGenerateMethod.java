package com.jtheories.core.generator.processor.arbitrary;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.GenerateMethod;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

public class ArbitraryGenerateMethod {

	MethodSpec generatedMethod;

	GeneratorInformation information;

	/**
	 * Generates an implementation for a generator method, defined in a generator interface
	 *
	 * @param information the declared generator interface information enclosed in a {@link
	 *     GeneratorInformation} object
	 * @param defaultMethod the method whose implementation needs to be generated
	 */
	public ArbitraryGenerateMethod(
		GeneratorInformation information,
		ExecutableElement defaultMethod
	) {
		this.information = information;

		var returnType = information.getReturnClassName();
		var generatedCode = generateCodeBlock(defaultMethod);
		var methodBuilder = MethodSpec
			.methodBuilder("generate")
			.addModifiers(Modifier.PUBLIC)
			.returns(returnType)
			.addCode(generatedCode);

		methodBuilder.addAnnotation(GenerateMethod.class);

		this.generatedMethod = methodBuilder.build();
	}

	/**
	 * Generates a code block implementing a generator method that calls its parent's default
	 * implementation with a generated value for each of its parameters
	 *
	 * @param defaultMethod the generator method implemented by default on the interface
	 * @return a {@link CodeBlock} containing the code for the generated implementation
	 */
	private CodeBlock generateCodeBlock(ExecutableElement defaultMethod) {
		var codeBlockBuilder = CodeBlock.builder();

		// Add assignment for each parameter
		defaultMethod
			.getParameters()
			.stream()
			.map(this::generateAssignment)
			.forEach(codeBlockBuilder::addStatement);

		// Create code block
		var codeBlock = defaultMethod
			.getParameters()
			.stream()
			.map(parameter -> CodeBlock.of("generated_$N", ParameterSpec.get(parameter)))
			.collect(CodeBlock.joining(", "));

		// Add return of parent's default method result
		codeBlockBuilder.addStatement(
			"return $T.super.$L($L)",
			this.information.getClassName(),
			defaultMethod.getSimpleName(),
			codeBlock
		);

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
	private CodeBlock generateAssignment(VariableElement parameter) {
		var annotationMirrors = parameter.getAnnotationMirrors();

		var parameterType = TypeName.get(parameter.asType());
		var parameterSpec = ParameterSpec.get(parameter);
		var typeArguments = ((DeclaredType) parameter.asType()).getTypeArguments();

		if (!typeArguments.isEmpty()) {
			return CodeBlock.of(
				"$T generated_$N = $T.getGenerator($T.class).generateConstrained($T.class)",
				parameterType,
				parameterSpec,
				Generators.class,
				this.information.getTypeUtils().erasure(parameter.asType()),
				typeArguments.get(0)
			);
		} else {
			if (annotationMirrors.isEmpty()) {
				return CodeBlock.of(
					"$T generated_$N = $T.gen($T.class)",
					parameterType,
					parameterSpec,
					Generators.class,
					parameterType
				);
			} else {
				List<CodeBlock> annotatedTypes = annotationMirrors
					.stream()
					.map(AnnotationMirror::getAnnotationType)
					.map(ClassName::get)
					.map(annotationType -> CodeBlock.of("$T.class", annotationType))
					.collect(Collectors.toList());

				return CodeBlock.of(
					"$T generated_$N = $T.gen($T.class, $L)",
					parameterType,
					parameterSpec,
					Generators.class,
					parameterType,
					CodeBlock.join(annotatedTypes, ", ")
				);
			}
		}
	}

	public MethodSpec getGeneratedMethod() {
		return generatedMethod;
	}
}
