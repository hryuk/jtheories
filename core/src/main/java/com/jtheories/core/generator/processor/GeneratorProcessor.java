package com.jtheories.core.generator.processor;

import com.google.auto.service.AutoService;
import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.exceptions.GeneratorProcessorException;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.jtheories.core.generator.processor.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractProcessor {

  private Messager messager = null;

  /**
   * Generates an implementation for a generator method, defined in a generator interface
   *
   * @param generatorInterface the generator interface
   * @param defaultMethod      the method whose implementation needs to be generated
   * @return a {@link MethodSpec} containing the method's generated implementation
   */
  private static MethodSpec generateMethodImplementation(
      TypeElement generatorInterface,
      ExecutableElement defaultMethod) {

    var generatedCode = generateCodeBlock(generatorInterface, defaultMethod);

    var annotationMirrors = defaultMethod.getAnnotationMirrors();
    String methodAnnotation = null;
    if (annotationMirrors.size() == 1) {
      methodAnnotation = TypeName.get(annotationMirrors.get(0).getAnnotationType()).toString();
    }

    var name = methodAnnotation == null
        ? "generate"
        : "generate" + methodAnnotation.substring(methodAnnotation.lastIndexOf('.') + 1);

    return MethodSpec.methodBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.get(getGenerateReturnType(generatorInterface)))
        .addCode(generatedCode)
        .build();
  }

  /**
   * Generates a code block implementing a generator method that calls its parent's default
   * implementation with a generated value for each of its parameters
   *
   * @param generatorInterface the interface annotated with {@link Generator}
   * @param defaultMethod      the generator method implemented by default on the interface
   * @return a {@link CodeBlock} containing the code for the generated implementation
   */
  private static CodeBlock generateCodeBlock(TypeElement generatorInterface,
      ExecutableElement defaultMethod) {
    var codeBlockBuilder = CodeBlock.builder();

    // Add assignment for each parameter
    defaultMethod.getParameters().stream()
        .map(GeneratorProcessor::generateAssignment)
        .forEach(codeBlockBuilder::addStatement);

    // Create code block
    var codeBlock = defaultMethod.getParameters().stream()
        .map(parameter -> CodeBlock.of("generated_$N", ParameterSpec.get(parameter)))
        .collect(CodeBlock.joining(", "));

    // Add return of parent's default method result
    codeBlockBuilder.addStatement(
        "return $T.super.$L($L)",
        TypeName.get(generatorInterface.asType()),
        defaultMethod.getSimpleName(),
        codeBlock);

    return codeBlockBuilder.build();
  }

  /**
   * Generates a random value assignment {@link CodeBlock} for a parameter. The generated code has
   * looks like this:
   * <p>{@code Parameter generated_parameter = Generators.gen(Parameter.class)}</p><br />
   * <p>If the parameter is annotated with a constraint, it will be taken into account, generating
   * code like this instead:</p>
   * <p>{@code Parameter generated_parameter = Generators.gen(Parameter.class,
   * Constraint.class)}</p>
   *
   * @param parameter a generator method's parameter represented by a {@link VariableElement}
   * @return a {@link CodeBlock} with the assignment code
   */
  private static CodeBlock generateAssignment(VariableElement parameter) {
    var annotationMirrors = parameter.getAnnotationMirrors();

    var parameterType = TypeName.get(parameter.asType());
    var parameterSpec = ParameterSpec.get(parameter);
    var parentInterface = ClassName.get(Generators.class);

    if (annotationMirrors.size() == 1) {
      return CodeBlock.of("$T generated_$N = $T.gen($T.class, $T.class)",
          parameterType,
          parameterSpec,
          parentInterface,
          parameterType,
          TypeName.get(annotationMirrors.get(0).getAnnotationType()));
    } else {
      return CodeBlock.of("$T generated_$N = $T.gen($T.class)",
          parameterType,
          parameterSpec,
          parentInterface,
          parameterType);
    }
  }

  // TODO: Document this
  private static TypeMirror getGenerateReturnType(TypeElement element) {
    return element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .map(Element::asType)
        .map(ExecutableType.class::cast)
        .map(ExecutableType::getReturnType)
        .findFirst()
        .orElseThrow();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> supportedAnnotations,
      RoundEnvironment roundEnv) {
    try {
      supportedAnnotations.stream()
          .map(roundEnv::getElementsAnnotatedWith)
          .flatMap(Collection::stream)
          .filter(this::checkAndReportIllegalUsages)
          .map(TypeElement.class::cast)
          .forEach(this::generateImplementation);
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
      fatal("Illegal use of @Generator on non-interface element %s",
          element.getSimpleName());
      return false;
    }

    return true;
  }

  /**
   * Given a {@link TypeElement} representing a generator interface, generate its implementation
   *
   * @param generatorInterface the interface to implement
   */
  private void generateImplementation(TypeElement generatorInterface) {
    var annotatedElementClassName = TypeName.get(generatorInterface.asType()).toString();
    var generatedClassName = TypeName.get(getGenerateReturnType(generatorInterface)).toString();
    var generatorPackage = annotatedElementClassName
        .substring(0, annotatedElementClassName.lastIndexOf('.'));
    var generatorClassName =
        String.format(
            "Arbitrary%sGenerator",
            generatedClassName.substring(generatedClassName.lastIndexOf('.') + 1));

    AnnotationSpec generatedAnnotation =
        AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", GeneratorProcessor.class.getName())
            .build();

    List<MethodSpec> methods = generatorInterface.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .map(ExecutableElement.class::cast)
        .map(executableElement -> generateMethodImplementation(generatorInterface,
            executableElement))
        .collect(Collectors.toList());

    TypeSpec arbitraryObject =
        TypeSpec.classBuilder(generatorClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(generatorInterface.asType()))
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(Generator.class),
                    TypeName.get(getGenerateReturnType(generatorInterface))))
            .addAnnotation(generatedAnnotation)
            .addMethods(methods)
            .build();

    var javaFile = JavaFile.builder(generatorPackage, arbitraryObject).build();
    String sourceFileName = generatorPackage + "." + generatorClassName;
    writeFile(sourceFileName, javaFile);
  }

  /**
   * Output a source file
   *
   * @param sourceFileName the source file's name
   * @param javaFile       the file content
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
