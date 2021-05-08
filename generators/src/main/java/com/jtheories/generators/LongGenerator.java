package com.jtheories.generators;

import com.jtheories.generator.processor.Generator;
import com.jtheories.random.SourceOfRandom;

@Generator
public interface LongGenerator{

    default Long generate(SourceOfRandom random) {
        return random.getRandom().nextLong();
    }
}