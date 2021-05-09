package com.jtheories.generators;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;
import java.util.UUID;

@Generator
public interface UUIDGenerator {

  default UUID generate(SourceOfRandom random) {
    return new UUID(random.getRandom().nextLong(), random.getRandom().nextLong());
  }
}
