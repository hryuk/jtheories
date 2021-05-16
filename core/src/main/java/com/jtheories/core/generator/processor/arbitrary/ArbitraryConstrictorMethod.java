package com.jtheories.core.generator.processor.arbitrary;

import com.jtheories.core.generator.processor.GenerateMethod;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class ArbitraryConstrictorMethod {

	private final MethodSpec generatedMethod;

	public ArbitraryConstrictorMethod(
		GeneratorInformation information,
		Element annotation,
		ExecutableElement defaultMethod
	) {
		var name = "generate" + annotation.getSimpleName();

		var methodBuilder = MethodSpec
			.methodBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.returns(information.getReturnClassName());

		for (VariableElement parameter : defaultMethod.getParameters()) {
			methodBuilder.addParameter(
				TypeName.get(parameter.asType()),
				String.format("arbitrary%s", information.getSimpleName(parameter.asType()))
			);
		}

		var methodCode = CodeBlock
			.builder()
			.addStatement(
				"return $T.super.$L($L)",
				TypeName.get(information.getGeneratorType().asType()),
				defaultMethod.getSimpleName(),
				defaultMethod
					.getParameters()
					.stream()
					.map(
						parameter ->
							CodeBlock.of("arbitrary$N", information.getSimpleName(parameter.asType()))
					)
					.collect(CodeBlock.joining(", "))
			)
			.build();

		methodBuilder.addCode(methodCode);

		methodBuilder.addAnnotation(GenerateMethod.class);

		this.generatedMethod = methodBuilder.build();
	}

	public MethodSpec getGeneratedMethod() {
		return generatedMethod;
	}
}
