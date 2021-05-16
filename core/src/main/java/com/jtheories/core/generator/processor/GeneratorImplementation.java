package com.jtheories.core.generator.processor;

import com.squareup.javapoet.JavaFile;

public abstract class GeneratorImplementation {

	protected GeneratorInformation information;
	protected JavaFile javaFile;
	protected String fileName;

	protected GeneratorImplementation(GeneratorInformation information) {
		this.information = information;
		this.fileName =
			String.format(
				"%s.%s",
				this.information.getGeneratorPackage(),
				this.information.getImplementerName()
			);
	}

	public String getFileName() {
		return this.fileName;
	}

	public JavaFile getJavaFile() {
		return this.javaFile;
	}
}
