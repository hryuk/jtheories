package com.jtheories.core.generator.processor.generic;

import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.processor.GeneratorImplementation;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class GenericGeneratorImplementation extends GeneratorImplementation {

	/**
	 * Given a {@link TypeElement} representing a generic generator interface, generate its
	 * implementation
	 *
	 * @param information the information about the interface to be implemented contained in a {@link
	 *                    GeneratorInformation} object
	 */
	public GenericGeneratorImplementation(GeneratorInformation information) {
		super(information);
	}

	@Override
	public JavaFile getJavaFile() {
		return JavaFile
			.builder(this.information.getGeneratorPackage(), this.implementGenerator())
			.build();
	}

	@Override
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

		typeBuilder.addTypeVariable(TypeVariableName.get("T"));

		return typeBuilder.build();
	}
}
