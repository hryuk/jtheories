package com.jtheories.core.runner;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.meta.TypeArgument;
import com.jtheories.core.random.SourceOfRandom;
import java.util.function.Consumer;

public abstract class Theory<T> {

	private Long numberOfTrials = 100L;

	private T value;

	public void check(final Consumer<T> property) {
		int i = 0;
		try {
			for (i = 0; i < this.numberOfTrials; i++) {
				this.checkOne(property);
			}
		} catch (Throwable e) {
			throw new AssertionError(
				String.format(
					"%nProperty falsified after %d trials%n\t%s%nFalsifying value:%s%nSeed: %d%n",
					i + 1,
					e,
					this.value,
					Generators.gen(new TypeArgument<>(SourceOfRandom.class)).getSeed()
				)
			);
		}
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

	protected T getValue() {
		return this.value;
	}

	protected void setValue(T value) {
		this.value = value;
	}
}
