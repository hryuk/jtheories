package com.jtheories.core.runner;

import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ TYPE_USE })
@Retention(RetentionPolicy.SOURCE)
public @interface Arbitrary {
}
