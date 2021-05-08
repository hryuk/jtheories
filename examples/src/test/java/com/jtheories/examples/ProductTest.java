package com.jtheories.examples;

import com.jtheories.core.junit.JTheories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

@ExtendWith(JTheories.class)
class ProductTest {

    @RepeatedTest(200)
    void doSomethingTest(Product product){
        Assertions.assertEquals(product.getId(),UUID.fromString(product.getId().toString()));
        Assertions.assertTrue(product.getPrice()<Long.MAX_VALUE);
    }
}
