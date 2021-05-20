package com.jtheories.core.generator.processor.generic;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.TypeArgument;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Supplier;
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
			.map(this::generateAssignment)
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

	private static boolean isPlainType(VariableElement parameter) {
		if (TypeKind.DECLARED.equals(parameter.asType().getKind())) {
			return ((DeclaredType) parameter.asType()).getTypeArguments().isEmpty();
		}
		if (TypeKind.TYPEVAR.equals(parameter.asType().getKind())) {
			return false;
		} else {
			throw new AssertionError("Poor panda :(");
		}
	}

	private static boolean isTypeVar(VariableElement parameter) {
		return TypeKind.TYPEVAR.equals(parameter.asType().getKind());
	}

	private CodeBlock generateAssignment(VariableElement parameter) {
		// Type of the parameter that will be assigned, i.e.: Collection<String>
		var parameterType = parameter.asType();
		// Parameter's type erasure, i.e.: Collection
		var typeErasure = this.information.getTypeUtils().erasure(parameterType);

		// The parameter is of a generic Supplier type, like Supplier<N>
		if (this.isSupplier(typeErasure)) {
			return CodeBlock.of(
				"$T $N = () -> $T.gen(typeArgument.getChildren()[$L])",
				typeErasure,
				ParameterSpec.get(parameter),
				Generators.class,
				0
			);
		}

		// The parameter is of a plain type, such as String
		if (isPlainType(parameter)) {
			return CodeBlock.of(
				"$T $N = ($T) $T.gen(new $T<>($T.class))",
				typeErasure,
				ParameterSpec.get(parameter),
				typeErasure,
				Generators.class,
				TypeArgument.class,
				typeErasure
			);
		}

		// The parameter is of a generic type, the likes of T
		if (isTypeVar(parameter)) {
			return CodeBlock.of(
				"$T $N = ($T) $T.gen(typeArgument.getChildren()[0])",
				typeErasure,
				ParameterSpec.get(parameter),
				typeErasure,
				Generators.class
			);
		}

		// Otherwise, the parameter is of a parameterized type like Collection<T>
		var typeArguments = ((DeclaredType) parameterType).getTypeArguments();

		return CodeBlock.of(
			"$T $N = ($T) $T.gen(new $T($T.class, $L))",
			typeErasure,
			ParameterSpec.get(parameter),
			typeErasure,
			Generators.class,
			TypeArgument.class,
			typeErasure,
			this.createChildrenTypeArgument(typeArguments)
		);
	}

	private CodeBlock createChildrenTypeArgument(List<? extends TypeMirror> typeArguments) {
		return CodeBlock.of(
			"new $T<?>[]{ $L }",
			TypeArgument.class,
			// TODO: Support meta-programmatic creation of recursive type parameters from this list
			// If TARGET GENERATOR is a parameterized class, we need to pass TARGET's CHILDREN instead of type args
			this.createChildTypeArgument(typeArguments.get(0))
		);
	}

	private CodeBlock createChildTypeArgument(TypeMirror typeMirror) {
		if (TypeKind.DECLARED.equals(typeMirror.getKind())) {
			// Argument is a declared type like String
			return CodeBlock.of("new $T<>($T.class)", TypeArgument.class, typeMirror);
		} else if (TypeKind.TYPEVAR.equals(typeMirror.getKind())) {
			// Argument is a type variable like T, use received type parameters' children
			return CodeBlock.of(
				"typeArgument.getChildren()[0]",
				TypeArgument.class,
				typeMirror
			);
		}
		throw new AssertionError("Saddest panda :(");
	}

	private boolean isSupplier(TypeMirror typeErasure) {
		var rawSupplier =
			this.information.getTypeUtils()
				.erasure(
					this.information.getElementUtils()
						.getTypeElement(Supplier.class.getName())
						.asType()
				);
		return this.information.getTypeUtils().isSameType(typeErasure, rawSupplier);
	}
}
