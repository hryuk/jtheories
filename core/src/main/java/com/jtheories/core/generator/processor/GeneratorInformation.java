package com.jtheories.core.generator.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.Objects;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class GeneratorInformation {

	private static final String ARBITRARY_TEMPLATE = "Arbitrary%sGenerator";
	private static final String GENERICS_TEMPLATE = "Generic%sGenerator";

	private final Types typeUtils;
	private final Elements elementUtils;
	private final TypeElement generatorType;
	private final ClassName className;
	private final TypeElement returnType;
	private final ClassName returnClassName;
	private final String generatorPackage;
	private final String implementerName;
	private final ExecutableElement defaultGenerateMethod;

	public GeneratorInformation(
		Types typeUtils,
		Elements elementUtils,
		TypeElement generatorType
	) {
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;
		this.generatorType = generatorType;
		this.returnType = this.getGeneratorReturnTypeElement(generatorType);
		this.returnClassName = ClassName.get(this.returnType);
		this.className = ClassName.get(generatorType);
		this.generatorPackage = this.className.packageName();
		this.implementerName =
			String.format(
				this.isParameterized() ? GENERICS_TEMPLATE : ARBITRARY_TEMPLATE,
				this.returnClassName.simpleName()
			);
		this.defaultGenerateMethod =
			this.generatorType.getEnclosedElements()
				.stream()
				.filter(e -> e.getKind() == ElementKind.METHOD)
				.filter(e -> e.getAnnotationMirrors().isEmpty())
				.map(ExecutableElement.class::cast)
				.findFirst()
				.orElseThrow();
	}

	public ClassName getReturnClassName() {
		return this.returnClassName;
	}

	/**
	 * Given a generator class represented by a {@link TypeElement}, obtain the return type of the
	 * generator
	 *
	 * @param element the class of the generator
	 * @return the return type of the generator as a {@link TypeMirror}
	 */
	private TypeElement getGeneratorReturnTypeElement(TypeElement element) {
		// TODO: This only works if the generator has only one return type among its methods
		//  (or only has one method), it will return any of its return types otherwise, we need to
		//  make sure that generators only have one return type or else make necessary arrangements to
		//  support more of them
		return element
			.getEnclosedElements()
			.stream()
			.filter(e -> e.getKind() == ElementKind.METHOD)
			.map(Element::asType)
			.map(ExecutableType.class::cast)
			.map(ExecutableType::getReturnType)
			.map(this.typeUtils::asElement)
			.filter(TypeElement.class::isInstance)
			.map(TypeElement.class::cast)
			.findFirst()
			.orElseThrow();
	}

	public ExecutableElement getDefaultGenerateMethod() {
		return this.defaultGenerateMethod;
	}

	public String getImplementerName() {
		return this.implementerName;
	}

	public String getGeneratorPackage() {
		return this.generatorPackage;
	}

	public ClassName getClassName() {
		return this.className;
	}

	public TypeElement getGeneratorType() {
		return this.generatorType;
	}

	public TypeElement getReturnType() {
		return this.returnType;
	}

	public boolean isParameterized() {
		return !this.getReturnType().getTypeParameters().isEmpty();
	}

	public String getSimpleName(TypeMirror type) {
		if (TypeName.get(type).isPrimitive()) {
			return TypeName.get(type).toString();
		} else {
			return this.typeUtils.asElement(type).getSimpleName().toString();
		}
	}

	public Types getTypeUtils() {
		return this.typeUtils;
	}

	public Elements getElementUtils() {
		return this.elementUtils;
	}
}
