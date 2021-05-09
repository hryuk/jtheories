package com.jtheories.generators.number;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface LongGenerator {

  default Long generate(SourceOfRandom random) {
    return random.getRandom().nextLong();
  }

  @Positive
  default Long generatePositive(SourceOfRandom random) {
    return Math.abs(random.getRandom().nextLong());
  }
}
