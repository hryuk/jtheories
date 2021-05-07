package com.jtheories.generators;

import com.jtheories.random.SourceOfRandom;

public interface Generator<T> {

    T generate(SourceOfRandom random);

}
