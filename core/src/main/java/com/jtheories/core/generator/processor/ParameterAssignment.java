package com.jtheories.core.generator.processor;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.TypeArgument;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParameterAssignment {

    private final GeneratorInformation information;
    private final CodeBlock codeBlock;

    ParameterAssignment(GeneratorInformation information, VariableElement parameter) {
        this.information = information;
        this.codeBlock = this.generateCodeBlock(parameter);
    }

    public CodeBlock getCodeBlock() {
        return this.codeBlock;
    }

    private static boolean isPlainType(TypeMirror type) {
        if (isTypeVar(type)) {
            return false;
        }
        if (TypeKind.DECLARED.equals(type.getKind())) {
            return ((DeclaredType) type).getTypeArguments().isEmpty();
        }
        throw new AssertionError("Unsupported parameter type");
    }

    private static boolean isTypeVar(TypeMirror type) {
        return TypeKind.TYPEVAR.equals(type.getKind());
    }

    // Return the index of a type variable in the generator's TypeArgument.children array
    private static int getChildIndex(ExecutableElement generateMethod, TypeMirror type) {
        if (!isTypeVar(type)) {
            throw new AssertionError(
                    "Expected type variable but got " + type.getKind() + " instead"
            );
        }
        var typeVariable = TypeVariableName.get((TypeVariable) type);
        var typeParameters = generateMethod
                .getTypeParameters()
                .stream()
                .map(TypeVariableName::get)
                .collect(Collectors.toList());
        var index = typeParameters.indexOf(typeVariable);
        if (index == -1) {
            throw new AssertionError(
                    "The method doesn't have a type parameter of this type, so you must be a wizard of some kind because that is not possible"
            );
        }
        return index;
    }

    private CodeBlock generateCodeBlock(VariableElement parameter) {
        // Type of the parameter that will be assigned, i.e.: Collection<String>
        var parameterType = parameter.asType();
        // Parameter's type erasure, i.e.: Collection
        var typeErasure = this.information.getTypeUtils().erasure(parameterType);

        // The parameter is of a plain type, such as String
        if (isPlainType(parameter.asType())) {
            return this.generatePlainTypeAssignment(parameter, parameterType);
        }

        // The parameter is of a generic type, the likes of T
        if (isTypeVar(parameter.asType())) {
            return this.generateTypeVarAssignment(parameter, typeErasure);
        }

        // The parameter is of a generic Supplier type, like Supplier<N>
        if (this.isSupplier(typeErasure)) {
            return this.generateSupplierAssignment(
                    parameter,
                    (DeclaredType) parameterType,
                    typeErasure
            );
        }

        // Otherwise, the parameter is of a parameterized type like Collection<T>
        return this.generateParameterizedTypeAssignment(
                parameter,
                (DeclaredType) parameterType,
                typeErasure
        );
    }

    private CodeBlock generateSupplierAssignment(
            VariableElement parameter,
            DeclaredType parameterType,
            TypeMirror typeErasure
    ) {
        // Suppliers have a single type parameter
        var supplierType = parameterType.getTypeArguments().get(0);
        if (isPlainType(supplierType)) {
            // Which can be a plain type like String
            return CodeBlock.of(
                    "$T $N = () -> $T.gen(new $T<>($T.class))",
                    typeErasure,
                    ParameterSpec.get(parameter),
                    Generators.class,
                    TypeArgument.class,
                    supplierType
            );
        }

        if (isTypeVar(supplierType)) {
            // Or a type variable like T
            return CodeBlock.of(
                    "$T $N = () -> $T.gen(typeArgument.getChildren()[$L])",
                    typeErasure,
                    ParameterSpec.get(parameter),
                    Generators.class,
                    getChildIndex(this.information.getDefaultGenerateMethod(), supplierType)
            );
        }

        throw new AssertionError("Supplier of unsupported type");
    }

    /**
     * This generates an assignment for a parameterized type, for example:
     * <p>
     * {@code Collection collection = (Collection) Generators.gen(new TypeArgument(Collection.class, ~appropriate children TypeArgument~));}
     *
     * @param parameter     the parameter itself
     * @param parameterType the parameter's type
     * @param typeErasure   the type erasure of the parameter
     * @return a {@link CodeBlock} for an assignment
     */
    private CodeBlock generateParameterizedTypeAssignment(
            VariableElement parameter,
            DeclaredType parameterType,
            TypeMirror typeErasure
    ) {
        return CodeBlock.of(
                "$T $N = ($T) $T.gen(new $T($T.class, $L))",
                typeErasure,
                ParameterSpec.get(parameter),
                typeErasure,
                Generators.class,
                TypeArgument.class,
                typeErasure,
                this.createChildrenTypeArgument(parameterType.getTypeArguments())
        );
    }

    /**
     * This generates an assignment for a type variable object, for example:
     * <p>
     * {@code Object t = (Object) Generators.gen(typeArgument.getChildren()[2]);}
     *
     * @param parameter   the parameter itself
     * @param typeErasure the type erasure of the parameter
     * @return a {@link CodeBlock} for an assignment
     */
    private CodeBlock generateTypeVarAssignment(
            VariableElement parameter,
            TypeMirror typeErasure
    ) {
        return CodeBlock.of(
                "$T $N = ($T) $T.gen(typeArgument.getChildren()[$L])",
                typeErasure,
                ParameterSpec.get(parameter),
                typeErasure,
                Generators.class,
                getChildIndex(this.information.getDefaultGenerateMethod(), parameter.asType())
        );
    }

    /**
     * This generates an assignment for a plain old type, for example:
     * <p>
     * {@code String string = (String) Generators.gen(new TypeArgument(String.class));}
     *
     * @param parameter     the parameter itself
     * @param parameterType the type erasure of the parameter
     * @return a {@link CodeBlock} for an assignment
     */
    private CodeBlock generatePlainTypeAssignment(
            VariableElement parameter,
            TypeMirror parameterType
    ) {
        return CodeBlock.of(
                "$T $N = ($T) $T.gen(new $T<>($T.class))",
                parameterType,
                ParameterSpec.get(parameter),
                parameterType,
                Generators.class,
                TypeArgument.class,
                parameterType
        );
    }

    private CodeBlock createChildrenTypeArgument(List<? extends TypeMirror> typeArguments) {
        return CodeBlock.of(
                "new $T<?>[]{ $L }",
                TypeArgument.class,
                typeArguments
                        .stream()
                        .map(this::createChildTypeArgument)
                        .collect(CodeBlock.joining(", "))
        );
    }

    private CodeBlock createChildTypeArgument(TypeMirror typeMirror) {
        if (TypeKind.DECLARED.equals(typeMirror.getKind())) {
            // Argument is a declared type like String
            return CodeBlock.of("new $T<>($T.class)", TypeArgument.class, typeMirror);
        } else if (TypeKind.TYPEVAR.equals(typeMirror.getKind())) {
            // Argument is a type variable like T, use received type parameters' children
            return CodeBlock.of(
                    "typeArgument.getChildren()[$L]",
                    getChildIndex(this.information.getDefaultGenerateMethod(), typeMirror)
            );
        }
        throw new AssertionError("Type argument is not a declared type nor a type variable");
    }

    private boolean isSupplier(TypeMirror typeErasure) {
        var rawSupplier =
                this.information.getTypeUtils()
                        .erasure(
                                this.information.getElementUtils()
                                        .getTypeElement(Supplier.class.getName())
                                        .asType()
                        );
        return this.information.getTypeUtils().isSameType(typeErasure, rawSupplier);
    }
}
