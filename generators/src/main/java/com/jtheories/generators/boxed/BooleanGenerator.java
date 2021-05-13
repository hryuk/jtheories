package com.jtheories.generators.boxed;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface BooleanGenerator {

  default Boolean generate(SourceOfRandom random) {
    return random.choice(null, Boolean.FALSE, Boolean.TRUE);
  }

}
