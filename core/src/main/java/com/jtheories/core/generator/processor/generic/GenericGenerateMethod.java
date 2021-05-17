package com.jtheories.core.generator.processor.generic;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.TypeArgument;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class GenericGenerateMethod {

	private final MethodSpec generateMethod;
	private final GeneratorInformation information;

	public GenericGenerateMethod(GeneratorInformation information) {
		this.information = information;

		var codeBlockBuilder = CodeBlock.builder();

		// Add assignment for each parameter
		this.information.getDefaultGenerateMethod()
			.getParameters()
			.stream()
			.map(this::generateAssignment)
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
				.addParameter(
					ParameterizedTypeName.get(List.class, TypeArgument.class),
					"typeArguments"
				)
				.addCode(codeBlockBuilder.build())
				.addStatement(
					"return $T.super.generate($L)",
					this.information.getClassName(),
					CodeBlock.join(paramNames, ", ")
				)
				.returns(this.information.getReturnClassName())
				.build();
	}

	public ClassName getReturnType() {
		return this.information.getReturnClassName();
	}

	public MethodSpec getGenerateMethod() {
		return this.generateMethod;
	}

	private CodeBlock generateAssignment(VariableElement parameter) {
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
				"$T $N = () -> (T)$T.getGenerator(typeArguments.get($L).getType()).generate($T.asList(typeArguments.get($L)))",
				parameter.asType(),
				ParameterSpec.get(parameter),
				Generators.class,
				0,
				Arrays.class,
				0
			);
		} else {
			return CodeBlock.of(
				"$T $N = ($T) $T.getGenerator($T.class).generate($T.asList(typeArguments.get($L)))",
				parameter.asType(),
				ParameterSpec.get(parameter),
				parameter.asType(),
				Generators.class,
				this.information.getTypeUtils().erasure(parameter.asType()),
				Arrays.class,
				0L
			);
		}
	}
}
