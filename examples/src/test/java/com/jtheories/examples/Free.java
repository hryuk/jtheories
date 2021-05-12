package com.jtheories.examples;

import com.jtheories.core.generator.processor.GeneratorConstraint;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({PARAMETER, METHOD})
@Retention(RUNTIME)
@GeneratorConstraint
public @interface Free {}
