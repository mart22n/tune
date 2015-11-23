package com.tune.businesslogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mart22n on 25.08.2015.
 */
public class RollingAverageFinder {

    private int nofValuesPerAverage = 1;
    private List<Double> internalVals = new ArrayList<>();
    public RollingAverageFinder(int nofValuesPerAverage) {
        this.nofValuesPerAverage = nofValuesPerAverage;
    }

    public void write(double value) {
        internalVals.add(value);
    }

    public List<Double> read() {
        List<Double> ret = new ArrayList<Double>();
        for (int i = 0; i < internalVals.size(); i += nofValuesPerAverage) {
            double avg = 0;
            int nofValuesPerCycle = 0;
            for (int j = i; j < Math.min(i + nofValuesPerAverage, internalVals.size()); ++j) {
                avg += internalVals.get(j);
                ++nofValuesPerCycle;
            }
            ret.add(avg / nofValuesPerCycle);
        }
        internalVals.clear();
        return ret;
    }
}