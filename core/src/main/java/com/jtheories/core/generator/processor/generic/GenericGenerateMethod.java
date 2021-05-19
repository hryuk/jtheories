package com.jtheories.core.generator.processor.generic;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.TypeArgument;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class GenericGenerateMethod {

	private final MethodSpec generateMethod;
	private final GeneratorInformation information;

	public GenericGenerateMethod(GeneratorInformation information) {
		this.information = information;

		var codeBlockBuilder = CodeBlock.builder();

		// Add assignment for each parameter
		var methodParams = this.information.getDefaultGenerateMethod().getParameters();
		IntStream
			.range(0, methodParams.size())
			.mapToObj(i -> this.generateAssignment(i, methodParams.get(i)))
			.forEach(codeBlockBuilder::addStatement);

		var paramNames =
			this.information.getDefaultGenerateMethod()
				.getParameters()
				.stream()
				.map(param -> CodeBlock.of(param.getSimpleName().toString()))
				.collect(Collectors.toList());

		this.generateMethod =
			MethodSpec
				.methodBuilder("generate")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(TypeName.get(TypeArgument.class), "typeArgument")
				.addCode(codeBlockBuilder.build())
				.addStatement(
					"return $T.super.generate($L)",
					this.information.getClassName(),
					CodeBlock.join(paramNames, ", ")
				)
				.returns(
					TypeName.get(this.information.getDefaultGenerateMethod().getReturnType())
				)
				.build();
	}

	public MethodSpec getGenerateMethod() {
		return this.generateMethod;
	}

	private CodeBlock generateAssignment(int index, VariableElement parameter) {
		TypeMirror supplierType =
			this.information.getTypeUtils()
				.erasure(
					this.information.getElementUtils()
						.getTypeElement(Supplier.class.getName())
						.asType()
				);
		TypeMirror parameterType =
			this.information.getTypeUtils().erasure(parameter.asType());

		if (this.information.getTypeUtils().isSameType(parameterType, supplierType)) {
			return CodeBlock.of(
				"$T $N = () -> (T)$T.gen(typeArgument.getChildren()[$L])",
				parameter.asType(),
				ParameterSpec.get(parameter),
				Generators.class,
				0
			);
		} else {
			if (((DeclaredType) parameter.asType()).getTypeArguments().isEmpty()) {
				return CodeBlock.of(
					"$T $N = ($T) $T.gen(new $T<>($T.class))",
					parameter.asType(),
					ParameterSpec.get(parameter),
					parameter.asType(),
					Generators.class,
					TypeArgument.class,
					this.information.getTypeUtils().erasure(parameter.asType())
				);
			} else {
				return CodeBlock.of(
					"$T $N = ($T)$T.gen(new $T($T.class, new $T<?>[]{ new $T<>($T.class)}))",
					parameter.asType(),
					ParameterSpec.get(parameter),
					parameter.asType(),
					Generators.class,
					TypeArgument.class,
					parameterType,
					TypeArgument.class,
					TypeArgument.class,
					// TODO: Support meta-programmatic creation of recursive type parameters from this list
					// If TARGET GENERATOR is a parameterized class, we need to pass TARGET's CHILDREN instead of type args
					((DeclaredType) parameter.asType()).getTypeArguments().get(0)
				);
			}
		}
	}
}
