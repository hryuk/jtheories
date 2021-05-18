package com.jtheories.core.generator.processor.arbitrary;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.TypeArgument;
import com.jtheories.core.generator.processor.GenerateMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public class ArbitraryGenerateMethod {

	private final MethodSpec constrainedMethod;
	private final ClassName returnType;

	public ArbitraryGenerateMethod(ClassName returnType) {
		this.returnType = returnType;
		var generatedClassName = returnType.simpleName();

		this.constrainedMethod =
			MethodSpec
				.methodBuilder("generate")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(
					ParameterizedTypeName.get(List.class, TypeArgument.class),
					"typeArguments"
				)
				.beginControlFlow("if(typeArguments.size()!=1)")
				.addStatement(
					"throw new AssertionError(\"Arbitrary generator called with more than one typeArgument\")"
				)
				.endControlFlow()
				.addStatement("Class[] annotations = typeArguments.get(0).getAnnotations()")
				.addStatement(
					"$T constrained$N = generateBasic()",
					returnType,
					generatedClassName
				)
				.addCode("\n")
				.addStatement(
					"$T<String, Object[]> methodParameters = new $T<>()",
					Map.class,
					HashMap.class
				)
				.beginControlFlow("if(annotations.length>0 )")
				.addStatement(
					"$T<Method> generatorMethods = Arrays.stream(this.getClass().getDeclaredMethods())\n" +
					"              .filter(m -> m.isAnnotationPresent($T.class))\n\n" +
					"              .collect($T.toList())",
					List.class,
					GenerateMethod.class,
					Collectors.class
				)
				.beginControlFlow("for($T method : generatorMethods)", Method.class)
				.addStatement(
					"Object[] parameterValues = $T.stream(method.getParameterTypes())\n" +
					"               .map($T::gen)\n" +
					"               .toArray(Object[]::new)",
					Arrays.class,
					Generators.class
				)
				.addStatement("methodParameters.put(method.getName(),parameterValues)")
				.endControlFlow()
				.endControlFlow()
				.addCode("\n")
				.beginControlFlow("for(Class annotation:annotations)")
				.addStatement(
					"String constrictorName = String.format(\"generate%s\",annotation.getSimpleName())"
				)
				.addStatement(
					"$T finalConstrained$N = constrained$N",
					returnType,
					generatedClassName,
					generatedClassName
				)
				.beginControlFlow("try")
				.addStatement(
					"constrained$N = ($T) Arrays.stream(this.getClass().getDeclaredMethods())\n" +
					"             .filter(m -> m.getName().equals(constrictorName))\n" +
					"             .findFirst()\n" +
					"             .orElseThrow(() -> new RuntimeException(this.getClass().getSimpleName()+\":\"+constrictorName))\n" +
					"             .invoke(this,\n" +
					"                 Arrays.stream(methodParameters.get(constrictorName))\n" +
					"                     .map(p -> p.getClass().equals($T.class)? finalConstrained$N :p)\n" +
					"                 .toArray(Object[]::new))",
					generatedClassName,
					returnType,
					returnType,
					generatedClassName
				)
				.nextControlFlow(
					"catch ($T|$T e)",
					IllegalAccessException.class,
					InvocationTargetException.class
				)
				.addStatement("throw new RuntimeException(\"Error calling generate method\",e)")
				.endControlFlow()
				.endControlFlow()
				.addCode("\n")
				.addStatement("return constrained$N", generatedClassName)
				.returns(returnType)
				.build();
	}

	public ClassName getReturnType() {
		return this.returnType;
	}

	public MethodSpec getGenerateMethod() {
		return this.constrainedMethod;
	}
}
