package com.jtheories.examples;

import com.jtheories.generators.Generator;
import com.jtheories.generators.Generators;
import com.jtheories.random.SourceOfRandom;


public class ProductGenerator implements Generator<Product> {

    @Override
    public Product generate(SourceOfRandom random) {
        return new Product(
                "id",
                Generators.gen(String.class,random),
                394L);
    }
}
