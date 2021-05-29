package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.exceptions.GeneratorProcessorException;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

public class JavaWritter {

	private final Filer filer;

	public JavaWritter(Filer filer) {
		this.filer = filer;
	}

	/**
	 * Output a source file
	 *
	 * @param generatorImplementation the generator implementation
	 * @throws GeneratorProcessorException if the file cannot be created
	 */
	public void writeFile(GeneratorImplementation generatorImplementation) {
		this.writeFile(
				generatorImplementation.getFileName(),
				generatorImplementation.getJavaFile()
			);
	}

	/**
	 * Output a source file
	 *
	 * @param sourceFileName the source file's name
	 * @param javaFile the file content
	 * @throws GeneratorProcessorException if the file cannot be created
	 */
	public void writeFile(String sourceFileName, JavaFile javaFile) {
		JavaFileObject builderFile;
		try {
			builderFile = this.filer.createSourceFile(sourceFileName);
		} catch (IOException e) {
			throw new GeneratorProcessorException(
				String.format("Error creating generated file %s", sourceFileName),
				e
			);
		}

		try (var out = new PrintWriter(builderFile.openWriter())) {
			javaFile.writeTo(out);
		} catch (IOException e) {
			throw new GeneratorProcessorException(
				String.format("Error writing generated file %s", sourceFileName),
				e
			);
		}
	}

	public void writeFile(String sourceFileName, JavaFile javaFile, List<String> imports) {
		JavaFileObject builderFile;
		try {
			builderFile = this.filer.createSourceFile(sourceFileName);
		} catch (IOException e) {
			throw new GeneratorProcessorException(
				String.format("Error creating generated file %s", sourceFileName),
				e
			);
		}

		try (var out = new PrintWriter(builderFile.openWriter())) {
			out.print(this.injectImports(javaFile, imports));
		} catch (IOException e) {
			throw new GeneratorProcessorException(
				String.format("Error writing generated file %s", sourceFileName),
				e
			);
		}
	}

	private String injectImports(JavaFile javaFile, List<String> imports) {
		String rawSource = javaFile.toString();

		List<String> result = new ArrayList<>();
		for (String s : rawSource.split("\n", -1)) {
			result.add(s);
			if (s.startsWith("package ")) {
				result.addAll(imports);
			}
		}
		return String.join("\n", result);
	}

	public Filer getFiler() {
		return this.filer;
	}
}
