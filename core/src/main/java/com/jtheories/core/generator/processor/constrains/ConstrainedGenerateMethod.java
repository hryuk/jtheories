package com.jtheories.core.generator.processor.constrains;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.meta.TypeArgument;
import com.jtheories.core.generator.meta.ValuedAnnotation;
import com.jtheories.core.generator.processor.GenerateMethod;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public class ConstrainedGenerateMethod {

	private final MethodSpec constrainedMethod;
	private final ClassName returnType;

	public ConstrainedGenerateMethod(GeneratorInformation information) {
		this.returnType = information.getReturnClassName();
		var generatedClassName = returnType.simpleName();

		this.constrainedMethod =
			MethodSpec
				.methodBuilder("generate")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(TypeName.get(TypeArgument.class), "typeArgument")
				.addStatement(
					"$T<$T> annotations = typeArgument.getAnnotations()",
					List.class,
					ValuedAnnotation.class
				)
				.addStatement(
					"$T constrained$N = generateBasic(typeArgument)",
					returnType,
					generatedClassName
				)
				.addCode("\n")
				.addStatement(
					"$T<String, Object[]> methodParameters = new $T<>()",
					Map.class,
					HashMap.class
				)
				.beginControlFlow("if(annotations.size() > 0 )")
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
					".filter($T.not(Class::isPrimitive) )\n" +
					".map(TypeArgument::new)\n" +
					".map($T::gen)\n" +
					".toArray(Object[]::new)",
					Arrays.class,
					Predicate.class,
					Generators.class
				)
				.addStatement("methodParameters.put(method.getName(),parameterValues)")
				.endControlFlow()
				.endControlFlow()
				.addCode("\n")
				.beginControlFlow("for($T annotation:annotations)", ValuedAnnotation.class)
				.addStatement(
					"String constrictorName = String.format(\"generate%s\",annotation.getAnnotation().getSimpleName())"
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
					"                 $T.concat(" +
					"Arrays.stream(methodParameters.get(constrictorName))\n" +
					"                     .map(p -> p.getClass().equals($T.class)? finalConstrained$N :p),\n" +
					"Arrays.stream(annotation.getValues().values().toArray()))\n" +
					"                 .toArray(Object[]::new))",
					generatedClassName,
					returnType,
					Stream.class,
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
