package com.jtheories.core.generator.processor.arbitrary;

import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.processor.GeneratorImplementation;
import com.jtheories.core.generator.processor.GeneratorInformation;
import com.jtheories.core.generator.processor.GeneratorProcessor;
import com.squareup.javapoet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.Generated;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ArbitraryGeneratorImplementation extends GeneratorImplementation {

	/**
	 * Given a {@link TypeElement} representing a generator interface, generate its implementation
	 *
	 * @param information the information about the interface to be implemented contained in a {@link
	 *                    GeneratorInformation} object
	 */
	public ArbitraryGeneratorImplementation(GeneratorInformation information) {
		super(information);
		this.javaFile =
			JavaFile
				.builder(this.information.getGeneratorPackage(), this.createArbitraryGenerator())
				.build();
	}

	private TypeSpec createArbitraryGenerator() {
		AnnotationSpec generatedAnnotation = AnnotationSpec
			.builder(Generated.class)
			.addMember("value", "$S", GeneratorProcessor.class.getName())
			.build();

		List<MethodSpec> generatorMethods = new ArrayList<>();

		MethodSpec generate =
			this.information.getGeneratorType()
				.getEnclosedElements()
				.stream()
				.filter(e -> e.getKind() == ElementKind.METHOD)
				.filter(e -> e.getAnnotationMirrors().isEmpty())
				.map(ExecutableElement.class::cast)
				.map(this::createArbitraryGenerateBasicMethod)
				.map(ArbitraryGenerateBasicMethod::getGenerateBasicMethod)
				.findAny()
				.orElseThrow();

		generatorMethods.add(generate);

		List<MethodSpec> constrictorMethods =
			this.information.getGeneratorType()
				.getEnclosedElements()
				.stream()
				.filter(e -> e.getKind() == ElementKind.METHOD)
				.filter(e -> e.getAnnotationMirrors().size() == 1)
				.map(ExecutableElement.class::cast)
				.map(this::createArbitraryConstrictorMethod)
				.map(ArbitraryConstrictorMethod::getGeneratedMethod)
				.collect(Collectors.toList());

		generatorMethods.addAll(constrictorMethods);

		generatorMethods.add(
			new ArbitraryGenerateMethod(this.information.getReturnClassName())
				.getGenerateMethod()
		);

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

	private ArbitraryGenerateBasicMethod createArbitraryGenerateBasicMethod(
		ExecutableElement executableElement
	) {
		return new ArbitraryGenerateBasicMethod(this.information, executableElement);
	}

	private ArbitraryConstrictorMethod createArbitraryConstrictorMethod(
		ExecutableElement executableElement
	) {
		var constrictorAnnotation = executableElement
			.getAnnotationMirrors()
			.get(0)
			.getAnnotationType()
			.asElement();
		return new ArbitraryConstrictorMethod(
			this.information,
			constrictorAnnotation,
			executableElement
		);
	}
}
