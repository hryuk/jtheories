package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.exceptions.GeneratorProcessorException;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.PrintWriter;
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
}
