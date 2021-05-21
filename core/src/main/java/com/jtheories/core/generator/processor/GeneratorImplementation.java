package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.processor.generic.GenericGenerateMethod;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class GeneratorImplementation {

	protected GeneratorInformation information;
	protected String fileName;
	protected JavaFile javaFile;

	/**
	 * Given a {@link TypeElement} representing a generic generator interface, generate its
	 * implementation
	 *
	 * @param information the information about the interface to be implemented contained in a {@link
	 *                    GeneratorInformation} object
	 */
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

	public String getFileName() {
		return this.fileName;
	}

	protected AnnotationSpec getGeneratedAnnotation() {
		return AnnotationSpec
			.builder(Generated.class)
			.addMember("value", "$S", GeneratorProcessor.class.getName())
			.addMember("date", "$S", OffsetDateTime.now().toString())
			.build();
	}

	public JavaFile getJavaFile() {
		return this.javaFile;
	}

	protected TypeSpec implementGenerator() {
		AnnotationSpec generatedAnnotation = this.getGeneratedAnnotation();

		List<MethodSpec> generatorMethods = new ArrayList<>();

		generatorMethods.add(new GenericGenerateMethod(this.information).getGenerateMethod());

		var typeBuilder = TypeSpec
			.classBuilder(this.information.getImplementerName())
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			.addSuperinterface(this.information.getClassName())
			.addSuperinterface(
				ParameterizedTypeName.get(
					ClassName.get(Generator.class),
					this.information.getReturnClassName()
				)
			)
			.addAnnotation(generatedAnnotation)
			.addMethods(generatorMethods);

		return typeBuilder.build();
	}
}
