package com.jtheories.core.runner.processor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.auto.service.AutoService;
import com.jtheories.core.generator.processor.JavaWritter;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class TheoryProcessor extends AbstractProcessor {

	private JavaWritter javaWritter;
	private JavaFileManager javaFileManager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		this.javaWritter = new JavaWritter(processingEnv.getFiler());
		this.javaFileManager = getJavaFileManager(processingEnv);
		var trees = Trees.instance(processingEnv);

		JavacTask
			.instance(processingEnv)
			.addTaskListener(
				new TaskListener() {
					@Override
					public void started(TaskEvent taskEvent) {
						// Nothing to do on task started event.
					}

					@Override
					public void finished(TaskEvent taskEvent) {
						if (taskEvent.getKind() == TaskEvent.Kind.ANALYZE) {
							var combinedTypeSolver = new CombinedTypeSolver();
							combinedTypeSolver.add(new ReflectionTypeSolver());

							var symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
							StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

							CompilationUnit cu = null;
							try {
								cu =
									StaticJavaParser.parse(
										Path.of(taskEvent.getCompilationUnit().getSourceFile().getName())
									);
							} catch (IOException e) {
								e.printStackTrace();
							}

							String className = taskEvent.getCompilationUnit().getSourceFile().getName();
							className = className.substring(0, className.lastIndexOf('.'));
							className = className.substring(className.lastIndexOf('/') + 1);

							Optional<ClassOrInterfaceDeclaration> clazz = cu.getClassByName(className);

							if (clazz.isPresent()) {
								for (MethodDeclaration method : clazz.get().getMethods()) {
									method.accept(
										new MethodCallVisitor(
											TheoryProcessor.this.javaWritter,
											taskEvent.getCompilationUnit(),
											TheoryProcessor.this.javaFileManager
										),
										null
									);
								}
							}
						}
					}
				}
			);
	}

	private static JavaFileManager getJavaFileManager(ProcessingEnvironment processingEnv) {
		JavaFileManager fileManager = null;

		try {
			var getContext = processingEnv.getClass().getMethod("getContext");
			Object context = getContext.invoke(processingEnv);
			var get = context.getClass().getMethod("get", Class.class);

			fileManager = (JavaFileManager) get.invoke(context, JavaFileManager.class);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return fileManager;
	}

	@Override
	public boolean process(
		Set<? extends TypeElement> supportedAnnotations,
		RoundEnvironment roundEnv
	) {
		return true;
	}
}
