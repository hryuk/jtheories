package com.jtheories.examples;

import com.jtheories.junit.JTheories;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JTheories.class)
class ProductTest {

    @RepeatedTest(200)
    void doSomethingTest(Product product){
        assertTrue(product.getPrice()>0);
    }
}
