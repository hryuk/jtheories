package com.jtheories.core.runner.processor;

import static java.util.Collections.singleton;

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
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

class MethodCallVisitor extends VoidVisitorAdapter<Void> {

	private final JavaWritter javaWritter;
	private final CompilationUnitTree compilationUnit;
	private final JavaFileManager javaFileManager;

	MethodCallVisitor(
		JavaWritter javaWritter,
		CompilationUnitTree compilationUnit,
		JavaFileManager javaFileManager
	) {
		this.javaWritter = javaWritter;
		this.compilationUnit = compilationUnit;
		this.javaFileManager = javaFileManager;
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
		String className = this.compilationUnit.getSourceFile().getName();

		className = className.substring(0, className.lastIndexOf('.'));
		className = className.substring(className.lastIndexOf('/') + 1);

		int lineNumber = methodCallExpr
			.getScope()
			.flatMap(Expression::toMethodCallExpr)
			.map(MethodCallExpr::getName)
			.map(simpleName -> simpleName.getBegin().map(begin -> begin.line).orElseThrow())
			.orElseThrow();

		CodeBlock buildArgsBlock = this.createBuildArgsBlock(args);

		var theoryClassName = String.format("Theory_%s_L%s", className, lineNumber);

		var theoryClassBuilder = TypeSpec.classBuilder(theoryClassName);
		theoryClassBuilder.addModifiers(Modifier.PUBLIC);

		theoryClassBuilder.superclass(
			ParameterizedTypeName.get(
				ClassName.get(Theory.class),
				ClassName.bestGuess(args.get(0).asClassOrInterfaceType().getName().toString())
			)
		);

		theoryClassBuilder.addField(
			FieldSpec
				.builder(
					ParameterizedTypeName.get(List.class, TypeArgument.class),
					"typeArguments",
					Modifier.PRIVATE,
					Modifier.FINAL
				)
				.initializer(CodeBlock.of("new $T<>()", ArrayList.class))
				.build()
		);

		theoryClassBuilder.addMethod(
			MethodSpec
				.constructorBuilder()
				.addCode(buildArgsBlock)
				.addModifiers(Modifier.PUBLIC)
				.build()
		);

		theoryClassBuilder.addMethod(
			MethodSpec
				.methodBuilder("checkOne")
				.addAnnotation(Override.class)
				.addParameter(
					ParameterSpec
						.builder(
							ParameterizedTypeName.get(
								ClassName.get(Consumer.class),
								ClassName.bestGuess(
									args.get(0).asClassOrInterfaceType().getName().toString()
								)
							),
							"property"
						)
						.build()
				)
				.addModifiers(Modifier.PROTECTED)
				.addStatement(
					CodeBlock.of(
						"property.accept(($L) $T.gen(typeArguments.get(0)))",
						ClassName.bestGuess(
							args.get(0).asClassOrInterfaceType().getName().toString()
						),
						Generators.class
					)
				)
				.build()
		);

		JavaFile theoryFile = JavaFile
			.builder(
				this.compilationUnit.getPackageName().toString(),
				theoryClassBuilder.build()
			)
			.build();

		URI generatedFile =
			this.javaWritter.writeFile(
					String.format(
						"%s.%s",
						this.compilationUnit.getPackageName().toString(),
						theoryClassName
					),
					theoryFile,
					this.compilationUnit.getImports()
						.stream()
						.map(Object::toString)
						.collect(Collectors.toList())
				);

		this.compileFile(generatedFile);
	}

	private void compileFile(URI fileName) {
		var compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
		try {
			compiler
				.getTask(
					null,
					this.javaFileManager,
					diagnosticCollector,
					new ArrayList<>(),
					null,
					singleton(
						new TheoryJavaFile(
							fileName,
							this.readFileContent(Paths.get(fileName).toString())
						)
					)
				)
				.call();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String readFileContent(String filePath) {
		try {
			return this.readFromInputStream(new FileInputStream(filePath));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file for compilation");
		}
	}

	private String readFromInputStream(InputStream inputStream) throws IOException {
		var resultStringBuilder = new StringBuilder();
		try (
			var bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8)
			)
		) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}
		return resultStringBuilder.toString();
	}

	private CodeBlock createBuildArgsBlock(NodeList<Type> args) {
		var codeBuilder = CodeBlock.builder();

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
								.map(a -> a.asClassOrInterfaceType().getName().toString())
								.forEach(
									a -> argList.add(CodeBlock.of("new TypeArgument<>($L.class)", a))
								)
					);

				codeBuilder.addStatement(
					"typeArguments.add(new $T($L.class,new $T<?>[]{$L}))",
					TypeArgument.class,
					arg.asClassOrInterfaceType().getName().toString(),
					TypeArgument.class,
					CodeBlock.join(argList, ",")
				);
			}
		);

		return codeBuilder.build();
	}
}
