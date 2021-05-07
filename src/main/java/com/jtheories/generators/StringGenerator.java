package com.jtheories.generators;

import com.jtheories.random.SourceOfRandom;

public class StringGenerator implements Generator<String>{
    private static final Long MAX_STRING_LENGTH=2048L;

    @Override
    public String generate(SourceOfRandom sourceOfRandom) {

        return sourceOfRandom.getRandom().ints(0x0000, 0xD7FF)
                .limit(MAX_STRING_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
