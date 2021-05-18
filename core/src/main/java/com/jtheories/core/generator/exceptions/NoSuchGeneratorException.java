package com.jtheories.core.generator.exceptions;

public class NoSuchGeneratorException extends RuntimeException {

	private static final long serialVersionUID = -4736458182980681963L;

	public NoSuchGeneratorException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public NoSuchGeneratorException(String msg) {
		super(msg);
	}
}
