package com.jtheories.examples;

public class VeryRealClass {

  private final Boolean aBoolean;
  private final Byte aByte;
  private final Character character;
  private final Short aShort;
  private final Integer integer;
  private final Long aLong;
  private final Float aFloat;
  private final Double aDouble;

  public VeryRealClass(
      Boolean aBoolean, Byte aByte, Character character, Short aShort,
      Integer integer, Long aLong, Float aFloat, Double aDouble) {
    this.aBoolean = aBoolean;
    this.aByte = aByte;
    this.character = character;
    this.aShort = aShort;
    this.integer = integer;
    this.aLong = aLong;
    this.aFloat = aFloat;
    this.aDouble = aDouble;
  }

  public Boolean getABoolean() {
    return aBoolean;
  }

  public Byte getAByte() {
    return aByte;
  }

  public Character getCharacter() {
    return character;
  }

  public Short getAShort() {
    return aShort;
  }

  public Integer getInteger() {
    return integer;
  }

  public Long getALong() {
    return aLong;
  }

  public Float getAFloat() {
    return aFloat;
  }

  public Double getADouble() {
    return aDouble;
  }
}
