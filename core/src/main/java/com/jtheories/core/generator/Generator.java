package com.jtheories.core.generator;

import com.jtheories.core.random.SourceOfRandom;

public interface Generator<T> {

    T generate(SourceOfRandom random);

}
