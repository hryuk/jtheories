package com.jtheories.examples;

public class VeryRealClass {

	private final Byte aByte;
	private final Character character;
	private final Short aShort;
	private final Integer integer;
	private final Long aLong;
	private final Float aFloat;
	private final Double aDouble;

	public VeryRealClass(
		Byte aByte,
		Character character,
		Short aShort,
		Integer integer,
		Long aLong,
		Float aFloat,
		Double aDouble
	) {
		this.aByte = aByte;
		this.character = character;
		this.aShort = aShort;
		this.integer = integer;
		this.aLong = aLong;
		this.aFloat = aFloat;
		this.aDouble = aDouble;
	}

	public Byte getAByte() {
		return this.aByte;
	}

	public Character getCharacter() {
		return this.character;
	}

	public Short getAShort() {
		return this.aShort;
	}

	public Integer getInteger() {
		return this.integer;
	}

	public Long getALong() {
		return this.aLong;
	}

	public Float getAFloat() {
		return this.aFloat;
	}

	public Double getADouble() {
		return this.aDouble;
	}
}
