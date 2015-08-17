package com.tune.tune;

import android.util.Pair;

import java.util.List;

/**
 * Created by mte on 13.08.2015.
 */
public class FrequencyExtractor {
    /**
     * Extracts a list of (frequency, duration) pairs from a waveform. The waveform consists of
     * sound pressure values. ASSUMPTION: sampling rate is 44100 samples/s!
     * @param waveform
     * @return
     */
    public List<Pair<Float, Short > > extractFrequenciesFromSample(double[] waveform) {
        throw new java.lang.UnsupportedOperationException("extractFrequenciesFromSample not implemented");
        //return new ArrayList<Pair<Float, Short> >();
    }
}
