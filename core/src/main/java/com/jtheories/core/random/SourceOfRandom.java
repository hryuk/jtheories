package com.jtheories.core.random;

import java.util.Random;

public class SourceOfRandom {

  private static final Random RANDOM = new Random();

  static {
    // We need to seed SourceOfRandom only once, or else test results cannot be reproduced
    RANDOM.setSeed(RANDOM.nextLong());
  }

  public Random getRandom() {
    return RANDOM;
  }

  /**
   * Randomly pick one of the given elements
   *
   * @param elements the elements to pick from
   * @param <T>      the type of the elements
   * @return one of the given elements at random
   */
  @SafeVarargs
  public final <T> T choice(T first, T... elements) {
    @SuppressWarnings("unchecked")
    var all = (T[]) new Object[elements.length + 1];
    all[0] = first;
    System.arraycopy(elements, 0, all, 1, elements.length);
    return all[RANDOM.nextInt(all.length)];
  }

}
