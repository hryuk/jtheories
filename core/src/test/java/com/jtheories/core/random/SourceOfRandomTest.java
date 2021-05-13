package com.jtheories.core.random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SourceOfRandomTest {

  @Test
  void choiceOfNullReturnsNullTest() {
    SourceOfRandom sourceOfRandom = new SourceOfRandom();

    Assertions.assertNull(sourceOfRandom.choice(null));
  }

  @Test
  void choiceOfOneElementReturnsElementTest() {
    SourceOfRandom sourceOfRandom = new SourceOfRandom();

    Object item = new Object();
    Assertions.assertSame(item, sourceOfRandom.choice(item));
  }

  @Test
  void choiceOfTwoElementsReturnsOneOfThemTest() {
    SourceOfRandom sourceOfRandom = new SourceOfRandom();

    Object item1 = new Object();
    Object item2 = new Object();
    Object result = sourceOfRandom.choice(item1, item2);
    Assertions.assertTrue(item1 == result || item2 == result);
  }

}
