package com.jtheories.core.generator.processor.constrains;

import com.jtheories.core.generator.exceptions.GeneratorProcessorException;
import com.jtheories.core.generator.processor.GenerateMethod;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class ConstrictorGenerateMethod {

	private final MethodSpec generatedMethod;

	public ConstrictorGenerateMethod(
		GeneratorInformation information,
		Element annotation,
		ExecutableElement defaultMethod
	) {
		var name = "generate" + annotation.getSimpleName();

		var methodBuilder = MethodSpec
			.methodBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.returns(information.getReturnClassName());

		var index = 0;
		for (VariableElement parameter : defaultMethod.getParameters()) {
			if (index == 0) {
				methodBuilder.addParameter(
					TypeName.get(parameter.asType()),
					String.format("arbitrary%s", information.getSimpleName(parameter.asType()))
				);
			} else {
				methodBuilder.addParameter(
					TypeName.get(parameter.asType()),
					String.format("annotationValue%d", index)
				);
			}
			index++;
		}

		var methodCode = CodeBlock
			.builder()
			.addStatement(
				"return $T.super.$L($L)",
				TypeName.get(information.getGeneratorType().asType()),
				defaultMethod.getSimpleName(),
				IntStream
					.range(0, defaultMethod.getParameters().size())
					.mapToObj(
						i -> {
							if (i == 0) {
								return CodeBlock.of(
									"arbitrary$N",
									information.getSimpleName(defaultMethod.getParameters().get(i).asType())
								);
							} else {
								return CodeBlock.of(String.format("annotationValue%d", i));
							}
						}
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
