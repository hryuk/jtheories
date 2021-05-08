package com.jtheories.generator;

import com.jtheories.random.SourceOfRandom;

public interface Generator<T> {

    T generate(SourceOfRandom random);

}
