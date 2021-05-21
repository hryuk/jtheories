package com.jtheories.examples;

public class ComplexGenerics<A, B> {

	private final A a;
	private final B b;

	public ComplexGenerics(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A getA() {
		return this.a;
	}

	public B getB() {
		return this.b;
	}
}
