package com.jtheories.core.generator.processor;

import com.google.auto.service.AutoService;
import com.jtheories.core.generator.Generator;
import com.jtheories.core.generator.Generators;
import com.squareup.javapoet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.jtheories.core.generator.processor.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractProcessor {

  private Messager messager = null;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
  }

  void fatal(String format, Object... args) {
    this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
      annotatedElements.forEach(
          element -> {
            if (element.getKind() != ElementKind.INTERFACE) {
              fatal(
                  "Illegal use of @Generator on non-interface element %s", element.getSimpleName());
            }
          });

      Collection<TypeElement> annotatedInterfaces =
          annotatedElements.stream().map(TypeElement.class::cast).collect(Collectors.toList());

      for (TypeElement annotatedInterface : annotatedInterfaces) {

        String annotatedElementClassName = TypeName.get(annotatedInterface.asType()).toString();
        String generatedClassName =
            TypeName.get(getGenerateReturnType(annotatedInterface)).toString();
        String generatedClassSimpleName =
            generatedClassName.substring(generatedClassName.lastIndexOf('.') + 1);
        String generatorPackage =
            annotatedElementClassName.substring(0, annotatedElementClassName.lastIndexOf('.'));
        String generatorClassName = String.format("Arbitrary%sGenerator", generatedClassSimpleName);

        List<ExecutableElement> defaultMethods =
            annotatedInterface.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .collect(Collectors.toList());

        List<MethodSpec> methods = new ArrayList<>();
        for (ExecutableElement defaultMethod : defaultMethods) {

          List<? extends VariableElement> parameters = defaultMethod.getParameters();
          CodeBlock.Builder generatedCode = CodeBlock.builder();

          List<CodeBlock> paramValues = new ArrayList<>();
          for (VariableElement parameter : parameters) {
            List<? extends AnnotationMirror> annotationMirrors = parameter.getAnnotationMirrors();
            if (annotationMirrors.isEmpty()) {
              generatedCode.addStatement(
                  "$T generated_$N = $T.gen($T.class)",
                  ClassName.get(parameter.asType()),
                  ParameterSpec.get(parameter),
                  ClassName.get(Generators.class),
                  ClassName.get(parameter.asType()));
            } else {
              List<CodeBlock> annotatedTypes =
                  annotationMirrors.stream()
                      .map(a -> CodeBlock.of("$T.class", ClassName.get(a.getAnnotationType())))
                      .collect(Collectors.toList());

              generatedCode.addStatement(
                  "$T generated_$N = $T.gen($T.class, $L)",
                  ClassName.get(parameter.asType()),
                  ParameterSpec.get(parameter),
                  ClassName.get(Generators.class),
                  ClassName.get(parameter.asType()),
                  CodeBlock.join(annotatedTypes, ", "));
            }

            paramValues.add(CodeBlock.of("generated_$N", ParameterSpec.get(parameter)));
          }

          generatedCode.addStatement(
              "return $T.super.$L($L)",
              ClassName.get(annotatedInterface.asType()),
              defaultMethod.getSimpleName(),
              CodeBlock.join(paramValues, ", ").toString());

          List<? extends AnnotationMirror> annotationMirrors = defaultMethod.getAnnotationMirrors();
          String methodAnnotation = null;
          if (annotationMirrors.size() == 1) {
            methodAnnotation =
                ClassName.get(annotationMirrors.get(0).getAnnotationType()).toString();
          }

          MethodSpec method;
          if (methodAnnotation == null) {
            method =
                MethodSpec.methodBuilder("generate")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.get(getGenerateReturnType(annotatedInterface)))
                    .addCode(generatedCode.build())
                    .build();
          } else {
            method =
                MethodSpec.methodBuilder(
                        "generate"
                            + methodAnnotation.substring(methodAnnotation.lastIndexOf('.') + 1))
                    .addParameter(
                        TypeName.get(getGenerateReturnType(annotatedInterface)),
                        String.format("arbitrary%s", generatedClassSimpleName))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.get(getGenerateReturnType(annotatedInterface)))
                    .addCode(
                        CodeBlock.builder()
                            .addStatement(
                                "return $T.super.$L($L)",
                                ClassName.get(annotatedInterface.asType()),
                                defaultMethod.getSimpleName(),
                                String.format("arbitrary%s", generatedClassSimpleName))
                            .build())
                    .build();
          }

          methods.add(method);
        }

        methods.add(getGenerateConstrainedMethod(getGenerateReturnType(annotatedInterface)));

        AnnotationSpec generatedAnnotation =
            AnnotationSpec.builder(Generated.class).addMember("value", "$S", "JTheories").build();

        TypeSpec arbitraryObject =
            TypeSpec.classBuilder(generatorClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(annotatedInterface.asType()))
                .addSuperinterface(
                    ParameterizedTypeName.get(
                        ClassName.get(Generator.class),
                        ClassName.get(getGenerateReturnType(annotatedInterface))))
                .addAnnotation(generatedAnnotation)
                .addMethods(methods)
                .build();

        JavaFile javaFile = JavaFile.builder(generatorPackage, arbitraryObject).build();

        JavaFileObject builderFile;
        String sourceFileName = generatorPackage + "." + generatorClassName;

        try {
          builderFile = processingEnv.getFiler().createSourceFile(sourceFileName);
        } catch (IOException e) {
          fatal("Error creating generated file %s", sourceFileName);
          return true;
        }
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
          javaFile.writeTo(out);
        } catch (IOException e) {
          fatal("Error writing generated file %s", sourceFileName);
        }
      }
    }

    return true;
  }

  private MethodSpec getGenerateConstrainedMethod(TypeMirror generatedType) {

    String generatedClassName =
        generatedType.toString().substring(generatedType.toString().lastIndexOf('.') + 1);

    return MethodSpec.methodBuilder("generateConstrained")
        .addModifiers(Modifier.PUBLIC)
        .addExceptions(
            Stream.of(
                    NoSuchMethodException.class,
                    InvocationTargetException.class,
                    IllegalAccessException.class)
                .map(TypeName::get)
                .collect(Collectors.toList()))
        .addParameter(Class[].class, "annotations")
        .varargs(true)
        .addStatement("$T constrained$N = generate()", generatedType, generatedClassName)
        .beginControlFlow("for(Class annotation:annotations)")
        .addStatement(
            "constrained$N = ($T)this.getClass()\n"
                + ".getDeclaredMethod(\"generate\"+annotation.getSimpleName(),$T.class)\n"
                + ".invoke(this,constrained$N) ",
            generatedClassName,
            generatedType,
            generatedType,
            generatedClassName)
        .endControlFlow()
        .addStatement("return constrained$N", generatedClassName)
        .returns(TypeName.get(generatedType))
        .build();
  }

  private TypeMirror getGenerateReturnType(TypeElement element) {
    return element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .map(Element::asType)
        .map(ExecutableType.class::cast)
        .map(ExecutableType::getReturnType)
        .findFirst()
        .orElseThrow();
  }
}
