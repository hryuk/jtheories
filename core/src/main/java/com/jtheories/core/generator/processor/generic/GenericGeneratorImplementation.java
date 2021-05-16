package com.jtheories.core.generator.processor.generic;

import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.processor.GeneratorImplementation;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.jtheories.core.generator.processor.GeneratorProcessor;
import com.squareup.javapoet.*;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class GenericGeneratorImplementation extends GeneratorImplementation {

	/**
	 * Given a {@link TypeElement} representing a generic generator interface, generate its
	 * implementation
	 *
	 * @param information the information about the interface to be implemented contained in a {@link
	 *     GeneratorInformation} object
	 */
	public GenericGeneratorImplementation(GeneratorInformation information) {
		super(information);
		this.javaFile =
			JavaFile
				.builder(this.information.getGeneratorPackage(), createArbitraryGenerator())
				.build();
	}

	private TypeSpec createArbitraryGenerator() {
		AnnotationSpec generatedAnnotation = AnnotationSpec
			.builder(Generated.class)
			.addMember("value", "$S", GeneratorProcessor.class.getName())
			.build();

		List<MethodSpec> generatorMethods = new ArrayList<>();

		generatorMethods.add(
			new GenericGenerateConstrainedMethod(information).getConstrainedMethod()
		);

		var typeBuilder = TypeSpec
			.classBuilder(information.getImplementerName())
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			.addSuperinterface(information.getClassName())
			.addSuperinterface(
				ParameterizedTypeName.get(
					ClassName.get(Generator.class),
					information.getReturnClassName()
				)
			)
			.addAnnotation(generatedAnnotation)
			.addMethods(generatorMethods);

		typeBuilder.addTypeVariable(TypeVariableName.get("T"));

		return typeBuilder.build();
	}
}
