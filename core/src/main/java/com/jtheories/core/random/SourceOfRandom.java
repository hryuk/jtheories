package com.jtheories.core.random;

import java.util.Random;

public class SourceOfRandom {

  private final Random random;

  public SourceOfRandom() {
    this.random = new Random();
    random.setSeed(random.nextLong());
  }

  public Random getRandom() {
    return random;
  }
}
