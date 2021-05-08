package com.jtheories.examples;

import com.jtheories.junit.JTheories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JTheories.class)
@Disabled
class ProductTest {

    @RepeatedTest(200)
    void doSomethingTest(Product product){
        Assertions.assertTrue(product.getPrice()>0);
    }
}
