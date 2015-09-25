package com.tune.businesslogic;
import org.junit.*;
import static org.junit.Assert.*;

public class BusinessLogicAdapterTest {
    protected int value1, value2;

    // assigning the values
    @Before
    public void setUp() {
        value1 = 3;
        value2 =4;
    }

    // test method to add two values
    @Test
    public void testAdd() {
        double result = value1 + value2;
        assertTrue(result ==7);
    }
}