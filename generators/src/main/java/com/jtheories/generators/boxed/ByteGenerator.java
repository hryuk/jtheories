package com.jtheories.generators.boxed;

import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

@Generator
public interface ByteGenerator {

  default Byte generate(SourceOfRandom random) {
    var bytes = new byte[1];
    random.getRandom().nextBytes(bytes);
    return bytes[0];
  }

}
