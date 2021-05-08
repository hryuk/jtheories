package com.jtheories.generators;

import com.jtheories.generator.processor.Generator;
import com.jtheories.random.SourceOfRandom;

import java.util.UUID;

@Generator
public interface UUIDGenerator {

    default UUID generate(SourceOfRandom random) {
        return new UUID(random.getRandom().nextLong(),random.getRandom().nextLong());
    }
}
