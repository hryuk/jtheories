package com.jtheories.examples;

import com.jtheories.generator.processor.Generator;
import com.jtheories.random.SourceOfRandom;
import java.util.UUID;
import static com.jtheories.generator.Generators.gen;

@Generator
public interface ProductGenerator {
    default Product generate(SourceOfRandom sourceOfRandom) {
        return new Product(
                gen(UUID.class),
                gen(String.class),
                gen(Long.class));
    }
}
