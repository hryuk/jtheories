package com.jtheories.core.runner.processor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.meta.TypeArgument;
import com.jtheories.core.generator.processor.JavaWritter;
import com.jtheories.core.runner.Theory;
import com.squareup.javapoet.*;
import com.sun.source.tree.CompilationUnitTree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

class MethodCallVisitor extends VoidVisitorAdapter<Void> {

	private final JavaWritter javaWritter;
	private final CompilationUnitTree compilationUnit;

	MethodCallVisitor(JavaWritter javaWritter, CompilationUnitTree compilationUnit) {
		this.javaWritter = javaWritter;
		this.compilationUnit = compilationUnit;
	}

	private boolean isJTheoriesCall(MethodCallExpr methodCallExpr) {
		return methodCallExpr
			.getScope()
			.flatMap(Expression::toMethodCallExpr)
			.flatMap(MethodCallExpr::getScope)
			.map(Expression::toString)
			.map("JTheories"::equals)
			.orElse(false);
	}

	private boolean isForAllCall(MethodCallExpr methodCallExpr) {
		return methodCallExpr
			.getScope()
			.flatMap(Expression::toMethodCallExpr)
			.map(MethodCallExpr::getName)
			.map(SimpleName::asString)
			.map("forAll"::equals)
			.orElse(false);
	}

	@Override
	public void visit(MethodCallExpr methodCallExpr, Void arg) {
		if (this.isJTheoriesCall(methodCallExpr) && this.isForAllCall(methodCallExpr)) {
			methodCallExpr
				.getScope()
				.flatMap(
					expression ->
						expression.toMethodCallExpr().flatMap(MethodCallExpr::getTypeArguments)
				)
				.ifPresent(args -> this.createTheory(methodCallExpr, args));
		}
		super.visit(methodCallExpr, arg);
	}

	private void createTheory(MethodCallExpr methodCallExpr, NodeList<Type> args) {
		String packageName =
			this.compilationUnit.getPackageName().toString().replace('.', '_');

		String className = this.compilationUnit.getSourceFile().getName();

		className = className.substring(0, className.lastIndexOf('.'));
		className = className.substring(className.lastIndexOf('/') + 1);

		int lineNumber = methodCallExpr.getBegin().get().line;

		CodeBlock buildArgsBlock = this.createBuildArgsBlock(args);

		var theoryClassName = String.format(
			"$Theory_%s_%s_L%s",
			packageName,
			className,
			lineNumber
		);

		this.javaWritter.writeFile(
				String.format(
					"%s.%s",
					this.compilationUnit.getPackageName().toString(),
					theoryClassName
				),
				JavaFile
					.builder(
						this.compilationUnit.getPackageName().toString(),
						TypeSpec
							.classBuilder(theoryClassName)
							.addSuperinterface(
								ParameterizedTypeName.get(
									ClassName.get(Theory.class),
									ClassName.bestGuess(args.get(0).asClassOrInterfaceType().toString())
								)
							)
							.addField(
								ClassName.bestGuess(args.get(0).asClassOrInterfaceType().toString()),
								"value",
								Modifier.PRIVATE,
								Modifier.FINAL
							)
							.addMethod(MethodSpec.constructorBuilder().addCode(buildArgsBlock).build())
							.addMethod(
								MethodSpec
									.methodBuilder("check")
									.addParameter(
										ParameterSpec
											.builder(
												ParameterizedTypeName.get(
													ClassName.get(Consumer.class),
													ClassName.bestGuess(
														args.get(0).asClassOrInterfaceType().toString()
													)
												),
												"property"
											)
											.build()
									)
									.addModifiers(Modifier.PUBLIC)
									.addStatement(CodeBlock.of("property.accept(this.value)"))
									.build()
							)
							.build()
					)
					.build(),
				this.compilationUnit.getImports()
					.stream()
					.map(Object::toString)
					.collect(Collectors.toList())
			);
	}

	private CodeBlock createBuildArgsBlock(NodeList<Type> args) {
		var codeBuilder = CodeBlock.builder();
		codeBuilder.addStatement(
			"$T<$T>typeArguments = new $T<>()",
			List.class,
			TypeArgument.class,
			ArrayList.class
		);
		Collection<CodeBlock> argList = new ArrayList<>();
		args.forEach(
			arg -> {
				arg
					.asClassOrInterfaceType()
					.getTypeArguments()
					.ifPresent(
						arguments ->
							arguments
								.stream()
								.map(a -> a.asClassOrInterfaceType().getName())
								.forEach(
									a ->
										argList.add(
											CodeBlock.of("new TypeArgument<>($L.class)", a.asString())
										)
								)
					);

				codeBuilder.addStatement(
					"typeArguments.add(new $T($L.class,new $T<?>[]{$L}))",
					TypeArgument.class,
					arg.asClassOrInterfaceType().getName(),
					TypeArgument.class,
					CodeBlock.join(argList, ",")
				);
			}
		);

		codeBuilder.addStatement(
			"this.value=($L) $T.gen(typeArguments.get(0))",
			ClassName.bestGuess(args.get(0).asClassOrInterfaceType().toString()),
			Generators.class
		);

		return codeBuilder.build();
	}
}
