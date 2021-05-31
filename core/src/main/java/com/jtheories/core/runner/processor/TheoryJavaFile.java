package com.jtheories.core.runner.processor;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

class TheoryJavaFile extends SimpleJavaFileObject {

	private final String sourceCode;

	TheoryJavaFile(URI className, String sourceCode) {
		super(className, Kind.SOURCE);
		this.sourceCode = sourceCode;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return this.sourceCode;
	}
}
