package com.tune;

import android.util.Pair;

import java.util.List;

/**
 * Created by mart22n on 22.08.2015.
 */
class FrequencyExtractor {

    private int sampleRate;
    private ReadingType readingType = ReadingType.ZERO_SAMPLES;
    private double loudnessThreshold = 30;

    public static enum ReadingType  {
        NO_PROBLEMS,
        TOO_QUIET,
        ZERO_SAMPLES,
        BIG_VARIANCE,
        BIG_FREQUENCY
    };

    public FrequencyExtractor(int sampleRate, int loudnessThreshold) {
        this.sampleRate = sampleRate;
        this.loudnessThreshold = loudnessThreshold;
    }

    double getFreqFromFiveCrossings(double[] pressureValues, int sampleSize){
        int crossings = 0;
        int curIndexOfCrossing = 0;
        int[] indexesOfCrossings = {-1, -1, -1, -1, -1};
        checkLoudness(pressureValues, sampleSize);
        if(readingType == ReadingType.TOO_QUIET)
            return -1;

        for(int i=0; i<sampleSize - 1; ++i) {
            if (valueMovesAwayFromZero(pressureValues, i)) {
                indexesOfCrossings[curIndexOfCrossing++] = i;
                ++crossings;
            }

            if(crossings == 5) {
                double avgCrossingInterval = (double)(indexesOfCrossings[4] - indexesOfCrossings[0] ) / 4;
                for(int j = 0; j < 3; ++j) {
                    if((indexesOfCrossings[j + 1] - indexesOfCrossings[j]) )
                }

                indexesOfCrossings[4] = i;
                break;
            }
        }

        if(crossings < 5) {
            throw new IllegalArgumentException("pressureValues");
        }

        return  sampleRate * (crossings - 1) / (double)(indexesOfCrossings[4] - indexesOfCrossings[0]) / 2;
    }

    private boolean valueMovesAwayFromZero(double[] pressureValues, int i) {
        return pressureValues[i] * pressureValues[i + 1] < 0 ||
                pressureValues[i+1] != 0 && pressureValues[i] == 0;
    }


    ReadingType readingType() {
        return readingType;
    }

    private void checkLoudness(double[] pressureValues, int sampleSize) {
        double loudness = 0.0;
        for(int i=0; i<sampleSize; ++i)
            loudness+=Math.abs(pressureValues[i]);
        loudness/=sampleSize;
        if(loudness < loudnessThreshold) readingType = ReadingType.TOO_QUIET;
        else readingType = ReadingType.NO_PROBLEMS;
    }

    List<Pair<Double, Short>> extractGroupsOfFrequencies(double[] pressureValues, int sampleSize) {
        int minGroupLenInMs = 20;   //TODO: put this to settings
        throw new UnsupportedOperationException();
    }


}
