package com.jtheories.examples;

import com.jtheories.generators.Generators;
import com.jtheories.generators.processor.Generator;
import com.jtheories.random.SourceOfRandom;


@Generator
public interface ProductGenerator {
    default Product generate(SourceOfRandom random) {
        return new Product(
                "id",
                Generators.gen(String.class,random),
                394L);

    }
}
