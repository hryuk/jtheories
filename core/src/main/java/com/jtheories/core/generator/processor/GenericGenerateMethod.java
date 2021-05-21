package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.TypeArgument;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.stream.Collectors;

public class GenericGenerateMethod {

	private final MethodSpec generateMethod;
	private final GeneratorInformation information;

	public GenericGenerateMethod(GeneratorInformation information) {
		this.information = information;

		var codeBlockBuilder = CodeBlock.builder();

		// Add assignment for each parameter
		var methodParams = this.information.getDefaultGenerateMethod().getParameters();
		methodParams
			.stream()
			.map(
				parameter -> new ParameterAssignment(this.information, parameter).getCodeBlock()
			)
			.forEach(codeBlockBuilder::addStatement);

		var paramNames =
			this.information.getDefaultGenerateMethod()
				.getParameters()
				.stream()
				.map(param -> CodeBlock.of(param.getSimpleName().toString()))
				.collect(Collectors.toList());

		var returnType = TypeName.get(
			this.information.getTypeUtils()
				.erasure(this.information.getDefaultGenerateMethod().getReturnType())
		);

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
				.returns(returnType)
				.build();
	}

	public MethodSpec getGenerateMethod() {
		return this.generateMethod;
	}
}
