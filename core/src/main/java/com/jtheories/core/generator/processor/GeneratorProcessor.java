package com.jtheories.core.generator.processor;

import com.google.auto.service.AutoService;
import com.jtheories.core.generator.exceptions.GeneratorProcessorException;
import com.jtheories.core.generator.processor.arbitrary.ArbitraryGeneratorImplementation;
import com.jtheories.core.generator.processor.generic.GenericGeneratorImplementation;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Set;

@SupportedAnnotationTypes("com.jtheories.core.generator.processor.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractProcessor {

  private Messager messager = null;
  private Types typeUtils;
  private JavaWritter javaWritter;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
    this.typeUtils = processingEnv.getTypeUtils();
    this.javaWritter = new JavaWritter(processingEnv.getFiler());
  }

  @Override
  public boolean process(
      Set<? extends TypeElement> supportedAnnotations, RoundEnvironment roundEnv) {
    try {
      supportedAnnotations.stream()
          .map(roundEnv::getElementsAnnotatedWith)
          .flatMap(Collection::stream)
          .filter(this::checkAndReportIllegalUsages)
          .map(TypeElement.class::cast)
          .map(typeElement -> new GeneratorInformation(typeUtils, typeElement))
          .map(
              info ->
                  info.isParameterized()
                      ? new GenericGeneratorImplementation(info)
                      : new ArbitraryGeneratorImplementation(info))
          .forEach(this.javaWritter::writeFile);
    } catch (GeneratorProcessorException e) {
      fatal(e.getMessage());
    }

    return true;
  }

  /**
   * If an {@link Element} is of a {@link ElementKind kind} other than {@link ElementKind#INTERFACE
   * INTERFACE}, inform about it and return false
   *
   * @param element the element to be examined
   * @return true if the element is an interface, false otherwise
   */
  private boolean checkAndReportIllegalUsages(Element element) {
    if (element.getKind() != ElementKind.INTERFACE) {
      fatal("Illegal use of @Generator on non-interface element %s", element.getSimpleName());
      return false;
    }

    return true;
  }

  private void fatal(String format, Object... args) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
  }
}
