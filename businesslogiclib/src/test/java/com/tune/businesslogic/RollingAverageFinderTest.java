package com.tune.businesslogic;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by mart22n on 19.11.2015.
 */
public class RollingAverageFinderTest extends TestBase {

    @Before
    public void setUp() {
        rollingAverageFinder = new RollingAverageFinder(3);
    }

    @Test
    public void whenNumberOfInputValuesEqualsNofValuesPerAverage_averageIsCorrect() {
        rollingAverageFinder.write(3);
        rollingAverageFinder.write(4);
        rollingAverageFinder.write(5);
        Assert.assertEquals(4.0, rollingAverageFinder.read().get(0));
    }

    @Test
    public void whenNumberOfInputValuesEqualsThreeTimesNofValuesPerAverage_averageIsCorrect() {
        rollingAverageFinder.write(3);
        rollingAverageFinder.write(4);
        rollingAverageFinder.write(5);

        rollingAverageFinder.write(6);
        rollingAverageFinder.write(7);
        rollingAverageFinder.write(8);

        rollingAverageFinder.write(9);
        rollingAverageFinder.write(10);
        rollingAverageFinder.write(11);
        List<Double> ret = rollingAverageFinder.read();
        Assert.assertEquals(4.0, ret.get(0));
        Assert.assertEquals(7.0, ret.get(1));
        Assert.assertEquals(10.0, ret.get(2));
    }

    @Test
    public void whenNumberOfInputValuesIsLessThanNofValuesPerAverage_averageIsCorrect() {
        rollingAverageFinder.write(3);
        rollingAverageFinder.write(4);

        List<Double> ret = rollingAverageFinder.read();
        Assert.assertEquals(3.5, ret.get(0));
    }

    @Test
    public void whenNumberOfInputValuesDoesNotDivideByNofValuesPerAverage_averageIsCorrect() {
        rollingAverageFinder.write(3);
        rollingAverageFinder.write(4);
        rollingAverageFinder.write(5);

        rollingAverageFinder.write(6);
        rollingAverageFinder.write(7);
        List<Double> ret = rollingAverageFinder.read();
        Assert.assertEquals(4.0, ret.get(0));
        Assert.assertEquals(6.5, ret.get(1));
    }

    @Test
    public void whenReadIsCalled_zeroIsReturnedWhenReadIsCalledSecondTime() {
        rollingAverageFinder.write(3);
        rollingAverageFinder.write(4);
        rollingAverageFinder.write(5);
        rollingAverageFinder.read();
        Assert.assertEquals(0, rollingAverageFinder.read().size());
    }
}
