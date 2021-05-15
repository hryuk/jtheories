package com.jtheories.core.random;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class allows for controlled randomness generators to be used in the library generators.
 * <p>
 * The class can be randomly reseeded at class level at any time vía {@link SourceOfRandom#reseed()}
 * or a seed can be specified using {@link SourceOfRandom#reseed(long)}.</p>
 * <p>
 * Instances of the class created between seedings will share the same {@link Random} instance that
 * can be accessed using {@link SourceOfRandom#getRandom()}. They will also inform of the seed that
 * was used to create that instance by way of {@link SourceOfRandom#getSeed()}.</p>
 * <p>
 * The class also implements some convenience methods to generate random results.</p>
 */
public class SourceOfRandom {

  // TODO: Check thread safety

  // Used for seeding, since seeds cannot be normally extracted from Random.class
  private static final Random SEEDER = new Random();
  // Keep the count of times this has been reseeded
  private static final AtomicLong counter = new AtomicLong();
  // Class level Random and seed
  private static Random parentRandom;
  private static long parentSeed;

  static {
    reseed();
  }

  // Instance level Random and seed, a copy of parent at the time of creation of the instance
  private final Random random;
  private final long seed;

  public SourceOfRandom() {
    this.random = parentRandom;
    this.seed = parentSeed;
  }

  public static void reseed() {
    reseed(SEEDER.nextLong());
  }

  public static void reseed(long seed) {
    parentSeed = seed;
    parentRandom = new Random(seed);
    counter.incrementAndGet();
  }

  public Random getRandom() {
    return this.random;
  }

  public long getSeed() {
    return this.seed;
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
    return all[this.getRandom().nextInt(all.length)];
  }

}
