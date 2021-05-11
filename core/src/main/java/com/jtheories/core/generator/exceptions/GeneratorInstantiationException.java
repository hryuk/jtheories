package com.jtheories.core.generator.exceptions;

public class GeneratorInstantiationException extends RuntimeException {

  public GeneratorInstantiationException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public GeneratorInstantiationException(String msg) {
    super(msg);
  }
}
