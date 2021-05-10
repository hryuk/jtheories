package com.jtheories.generators.number;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.jtheories.core.generator.processor.GeneratorConstraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Constrains the generator to strictly positive (>0) values
 */
@Target({PARAMETER, METHOD})
@Retention(RUNTIME)
@GeneratorConstraint
public @interface Positive {

}
