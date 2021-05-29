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
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class TheoryProcessor extends AbstractProcessor {

	private JavaWritter javaWritter;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.javaWritter = new JavaWritter(processingEnv.getFiler());
		Trees trees = Trees.instance(processingEnv);

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
							CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
							combinedTypeSolver.add(new ReflectionTypeSolver());

							JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
							StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

							CompilationUnit cu = StaticJavaParser.parse(
								taskEvent.getCompilationUnit().toString()
							);

							Optional<ClassOrInterfaceDeclaration> classX = cu.getClassByName(
								"ProductTest"
							);

							if (classX.isPresent()) {
								for (MethodDeclaration method : classX.get().getMethods()) {
									// Make the visitor go through everything inside the method.
									method.accept(
										new MethodCallVisitor(
											TheoryProcessor.this.javaWritter,
											taskEvent.getCompilationUnit()
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

	@Override
	public boolean process(
		Set<? extends TypeElement> supportedAnnotations,
		RoundEnvironment roundEnv
	) {
		return true;
	}
}
