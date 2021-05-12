package com.jtheories.core.generator.processor;

import com.google.auto.service.AutoService;
import com.jtheories.core.generator.exceptions.GeneratorProcessorException;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.jtheories.core.generator.processor.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractProcessor {

  private Messager messager = null;
  private Types typeUtils;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
    this.typeUtils = processingEnv.getTypeUtils();
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
          .map(
              (annotatedElement) ->
                  new ArbitraryGeneratorImplementation(annotatedElement, typeUtils))
          .forEach(
              arbitraryGenerator ->
                  writeFile(arbitraryGenerator.getFileName(), arbitraryGenerator.getJavaFile()));

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

  /**
   * Output a source file
   *
   * @param sourceFileName the source file's name
   * @param javaFile the file content
   * @throws GeneratorProcessorException if the file cannot be created
   */
  private void writeFile(String sourceFileName, JavaFile javaFile) {
    JavaFileObject builderFile;
    try {
      builderFile = this.processingEnv.getFiler().createSourceFile(sourceFileName);
    } catch (IOException e) {
      throw new GeneratorProcessorException(
          String.format("Error creating generated file %s", sourceFileName), e);
    }

    try (var out = new PrintWriter(builderFile.openWriter())) {
      javaFile.writeTo(out);
    } catch (IOException e) {
      throw new GeneratorProcessorException(
          String.format("Error writing generated file %s", sourceFileName), e);
    }
  }

  private void fatal(String format, Object... args) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
  }
}
