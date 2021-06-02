package com.jtheories.core.runner;

import java.util.function.Consumer;
import java.util.stream.LongStream;

public abstract class Theory<T> {

	private Long numberOfTrials = 200L;

	public void check(final Consumer<T> property) {
		LongStream.range(0, this.numberOfTrials).forEach(i -> this.checkOne(property));
	}

	public void run(final Consumer<T> property) {
		this.checkOne(property);
	}

	public Long getNumberOfTrials() {
		return this.numberOfTrials;
	}

	public void setNumberOfTrials(Long numberOfTrials) {
		this.numberOfTrials = numberOfTrials;
	}

	protected abstract void checkOne(final Consumer<T> property);
}
