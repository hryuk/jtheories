package com.jtheories.generators;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface VoidGenerator {

  default Void generate(SourceOfRandom random) {
    return null;
  }
}
