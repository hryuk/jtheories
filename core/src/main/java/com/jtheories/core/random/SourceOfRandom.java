package com.jtheories.core.random;

import java.util.Random;

public class SourceOfRandom {

  private static final Random random = new Random();

  static {
    // We need to seed SourceOfRandom only once, or else test results cannot be reproduced
    random.setSeed(random.nextLong());
  }

  public Random getRandom() {
    return random;
  }
}
