package com.jtheories.generators.boxed;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface CharacterGenerator {

  default Character generate(SourceOfRandom random) {
    var bytes = new byte[Character.BYTES];
    random.getRandom().nextBytes(bytes);
    return (char) ((bytes[0] << 8) + (bytes[1] & 0xff));
  }
}
