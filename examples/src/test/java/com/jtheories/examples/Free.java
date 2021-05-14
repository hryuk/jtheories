package com.jtheories.examples;

import com.jtheories.core.generator.processor.GeneratorConstrain;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({PARAMETER, METHOD, TYPE_USE})
@Retention(RUNTIME)
@GeneratorConstrain
public @interface Free {}
