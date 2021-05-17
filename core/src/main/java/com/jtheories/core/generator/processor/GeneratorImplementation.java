package com.jtheories.core.generator.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;

public abstract class GeneratorImplementation {

	protected GeneratorInformation information;
	protected String fileName;
	protected JavaFile javaFile;

	protected GeneratorImplementation(GeneratorInformation information) {
		this.information = information;
		this.fileName =
			String.format(
				"%s.%s",
				this.information.getGeneratorPackage(),
				this.information.getImplementerName()
			);

		this.javaFile =
			JavaFile
				.builder(this.information.getGeneratorPackage(), this.implementGenerator())
				.build();
	}

	public JavaFile getJavaFile() {
		return this.javaFile;
	}

	public String getFileName() {
		return this.fileName;
	}

	protected abstract TypeSpec implementGenerator();

	protected AnnotationSpec getGeneratedAnnotation() {
		return AnnotationSpec
			.builder(Generated.class)
			.addMember("value", "$S", GeneratorProcessor.class.getName())
			.addMember("date", "$S", OffsetDateTime.now().toString())
			.build();
	}
}
