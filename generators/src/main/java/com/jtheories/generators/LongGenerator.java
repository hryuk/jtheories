package com.jtheories.generators;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface LongGenerator {

  default Long generate(SourceOfRandom random) {
    return random.getRandom().nextLong();
  }
}
