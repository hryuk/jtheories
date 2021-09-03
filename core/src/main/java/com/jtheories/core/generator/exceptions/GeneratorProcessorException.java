package com.jtheories.core.generator.exceptions;

public class GeneratorProcessorException extends RuntimeException {

	public GeneratorProcessorException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public GeneratorProcessorException(String msg) {
		super(msg);
	}
}
