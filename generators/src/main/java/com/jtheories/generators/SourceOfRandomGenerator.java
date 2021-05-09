package com.jtheories.generators;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface SourceOfRandomGenerator {
  default SourceOfRandom generate() {
    return new SourceOfRandom();
  }
}
