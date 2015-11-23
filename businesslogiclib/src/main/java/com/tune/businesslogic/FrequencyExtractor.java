package com.tune.businesslogic;

import java.util.Observable;

/**
 * Created by mart22n on 22.08.2015.
 */
public class FrequencyExtractor extends Observable {

    private int sampleRate;
    private ReadingType readingType = ReadingType.ERROR;
    private double loudnessThreshold = 60;
    private int nofConsecutiveUpwardsCrossingsToMeasure = 4;
    private double measurementWindowMs = 30;
    private int samplesLength;
    private int gapBetweenSamplesWhenDetectingPause;

    private double[] prevInputsLastWindowSamples;
    private int nofSamplesInWindow;
    int nofSamplesInPrevInputsLastWindow;



    // maxDifferenceOfOneZeroCrossingIntervalFromAverageCrossingIntervalInPercent
    private double maxDiffInPercent = 1;

    static enum ReadingType  {
        OK,
        ONLY_SINGLE_PARTIAL_WINDOW_IN_INPUT,
        ERROR
    };

    public static class FrequencyExtractorSettings extends Observable {
        public int loudnessThreshold, gapBetweenSamplesWhenDetectingPause, sampleRate;
        public int nofConsecutiveUpwardsCrossingsToMeasure;
        public double measurementWindowMs, maxDiffInPercent;

        void setNofConsecutiveUpwardsCrossingsToMeasure(int value) {
            try {
                if (value < 4) {
                    nofConsecutiveUpwardsCrossingsToMeasure = 4;
                    throw new IllegalArgumentException();
                } else
                    nofConsecutiveUpwardsCrossingsToMeasure = value;
            } catch (IllegalArgumentException e) {
                notifyObservers("Illegal param value: sampleRate = 44100, " +
                        "nofConsecutiveUpwardsCrossingsToMeasure >=4," +
                        "measurementWindowMs <= 100. Using default values.");
            }
        }
    }

    FrequencyExtractor(FrequencyExtractorSettings settings) {
        this.sampleRate = settings.sampleRate;
        this.loudnessThreshold = settings.loudnessThreshold;
        this.nofConsecutiveUpwardsCrossingsToMeasure = settings.nofConsecutiveUpwardsCrossingsToMeasure;
        this.measurementWindowMs = settings.measurementWindowMs;
        this.maxDiffInPercent = settings.maxDiffInPercent;
        this.gapBetweenSamplesWhenDetectingPause = settings.gapBetweenSamplesWhenDetectingPause;

        nofSamplesInWindow = (int)(measurementWindowMs * (double)sampleRate / 1000.0);
        prevInputsLastWindowSamples = new double[nofSamplesInWindow];
    }

            /**
             * Takes in array of sound pressure values, gives back array of frequencies. Each frequency
             * in the array is measured for a measurement interval specified by measurementWindowMs.
             * When the array of previous samples ended at e.g. 2/3 of a measurement interval, the last
             * measurement window in previous samples array is concatenated with the first measurement
             * window in new samples array.
             * @param samples
             * @param samplesBeforeAutoCorrelation - original samples from audiorecord, used for
             *                                     measuring loudness
             * @return
             */
    public double[] extractFrequencies(double[] samples, double[] samplesBeforeAutoCorrelation, int len) {
        int windowIndex = 0;
        samplesLength = len;
        if(samplesLength < nofSamplesInWindow) {
            readingType = ReadingType.ONLY_SINGLE_PARTIAL_WINDOW_IN_INPUT;
            return new double[] {-1};
        }

        // samples which are left over from last window are discarded from ret
        double[] ret = new double[samplesLength / nofSamplesInWindow];
        int offsetInSamples = 0;

        if(nofSamplesInPrevInputsLastWindow > 0) {
            double[] concatenatedWindow = new double[nofSamplesInWindow];
            double[] concatenatedWindowForpauses = new double[nofSamplesInWindow];
            offsetInSamples = nofSamplesInWindow - nofSamplesInPrevInputsLastWindow;
            if(finalWindowIsFullLengthInCurrentInput(offsetInSamples))
                ret = new double[ret.length + 1];
            concatSamplesFromPrevInputIntoFirstWindowOfCurSamples(samples, nofSamplesInWindow, concatenatedWindow);
            concatSamplesFromPrevInputIntoFirstWindowOfCurSamples(samplesBeforeAutoCorrelation, nofSamplesInWindow, concatenatedWindowForpauses);
            if(isPause(concatenatedWindowForpauses, 0) == true) {
                ret[windowIndex++] = 0;
            }
            else {
                ret[windowIndex++] = findNonzeroFreqInWindow(concatenatedWindow, 0);
            }
        }

        findFreqForEachWindow(samples, samplesBeforeAutoCorrelation, ret, windowIndex, offsetInSamples);
        readingType = ReadingType.OK;
        return ret;
    }

    private boolean finalWindowIsFullLengthInCurrentInput(int offsetInSamples) {
        return (samplesLength - offsetInSamples) % nofSamplesInWindow == 0;
    }

    private void findFreqForEachWindow(double[] samples, double[] samplesBeforeAutocorrelation, double[] ret, int windowIndex, int offsetOfFirstWindowInSamples) {
        for(int offsetOfWindowInSamples = offsetOfFirstWindowInSamples; offsetOfWindowInSamples <
                samplesLength; offsetOfWindowInSamples += nofSamplesInWindow) {

            if(inputsLastWindowIsPartial(samples, offsetOfWindowInSamples)) {
                storeLastPartialWindow(samples, offsetOfWindowInSamples);
                break;
            }
            else {
                if(windowIndex == ret.length) {
                    double[] tmp = new double[ret.length];
                    System.arraycopy(ret, 0, tmp, 0, ret.length);
                    ret = new double[tmp.length + 1];
                    System.arraycopy(tmp, 0, ret, 0, tmp.length);
                }
                if (isPause(samplesBeforeAutocorrelation, offsetOfWindowInSamples) == true)
                    ret[windowIndex++] = 0;
                else
                    ret[windowIndex++] = findNonzeroFreqInWindow(samples, offsetOfWindowInSamples);
            }
        }
    }

    private boolean inputsLastWindowIsPartial(double[] samples, int offsetOfWindowInSamples) {
        return samplesLength < offsetOfWindowInSamples + nofSamplesInWindow;
    }

    private void concatSamplesFromPrevInputIntoFirstWindowOfCurSamples(double[] samples, int nofSamplesInWindow, double[] firstWindow) {
        System.arraycopy(prevInputsLastWindowSamples, 0, firstWindow, 0, nofSamplesInPrevInputsLastWindow);
        System.arraycopy(samples, 0, firstWindow, nofSamplesInPrevInputsLastWindow,
                nofSamplesInWindow - nofSamplesInPrevInputsLastWindow);
    }

    private void storeLastPartialWindow(double[] samples, int offsetInInput) {
        nofSamplesInPrevInputsLastWindow = samplesLength - offsetInInput;
            System.arraycopy(samples, offsetInInput, prevInputsLastWindowSamples, 0,
                    nofSamplesInPrevInputsLastWindow);
    }

    private double findNonzeroFreqInWindow(double[] samples, int offsetInSamples) {
        int[] indexesOfCrossings = new int[nofConsecutiveUpwardsCrossingsToMeasure];
        boolean crossingsWithEqualIntervalsFound;
        double ret;

        crossingsWithEqualIntervalsFound = findCrossingsWithEqualIntervals(samples, offsetInSamples,
                indexesOfCrossings);

        if(crossingsWithEqualIntervalsFound == true) {
            ret = (double) sampleRate * (double) (nofConsecutiveUpwardsCrossingsToMeasure - 1) /
                    (double) (indexesOfCrossings[nofConsecutiveUpwardsCrossingsToMeasure - 1] - indexesOfCrossings[0]);
            if (ret > 1200) { // max 1200 hz
                ret = -1;
            }
        }
        else {
            ret = -1;
        }
        return ret;
    }

    private boolean findCrossingsWithEqualIntervals(double[] samples, int offsetInSamples,
                                                    int[] indexesOfCrossings) {
        boolean ret;
        boolean crossingsWithEqualIntervalsFound = false;
        int nofCrossings = 0;
        int curIndexOfCrossing = 0;
        for (int i = offsetInSamples; i < offsetInSamples + nofSamplesInWindow - 1; ++i) {
            if (valueMovesUpwardsFromZero(samples, i)) {
                indexesOfCrossings[curIndexOfCrossing++] = i + 1;
                ++nofCrossings;
            }

            if (allCrossingsDetected(nofCrossings)) {
                double avgCrossingInterval = getAvgCrossingInterval(nofCrossings, indexesOfCrossings);

                if(thereIsACrossingIntervalThatDiffersTooMuchFromAvg(indexesOfCrossings, avgCrossingInterval) == false) {
                    indexesOfCrossings[nofConsecutiveUpwardsCrossingsToMeasure - 1] = i + 1;
                    crossingsWithEqualIntervalsFound = true;
                }
                break;
            }
        }
        ret = crossingsWithEqualIntervalsFound;
        return ret;
    }

    private double getAvgCrossingInterval(int nofCrossings, int[] indexesOfCrossings) {
        double avgCrossingInterval = 0;
        int sum = 0;
        for(int k = 0; k < nofConsecutiveUpwardsCrossingsToMeasure - 1; ++k) {
            sum += indexesOfCrossings[k + 1] - indexesOfCrossings[k];
        }
        avgCrossingInterval = (double)sum / (nofCrossings - 1);
        return avgCrossingInterval;
    }

    private boolean allCrossingsDetected(int crossings) {
        return crossings == nofConsecutiveUpwardsCrossingsToMeasure;
    }

    private boolean thereIsACrossingIntervalThatDiffersTooMuchFromAvg(int[] indexesOfCrossings, double avgCrossingInterval) {
        for (int j = 0; j < nofConsecutiveUpwardsCrossingsToMeasure - 1; ++j) {
            if ((100 - (double) (indexesOfCrossings[j + 1] - indexesOfCrossings[j]) / avgCrossingInterval * 100) > maxDiffInPercent) {
            return true; }
        }
        return false;
    }

    private boolean valueMovesUpwardsFromZero(double[] samples, int i) {
        return (samples[i] < 0 || samples[i] == 0) && samples[i + 1] > 0;
    }


    ReadingType readingType() {
        return this.readingType;
    }

    public boolean isPause(double[] samples, int offset) {
        double loudness = 0.0;
        for(int i=offset; i< Math.min(offset + nofSamplesInWindow, samplesLength); i += gapBetweenSamplesWhenDetectingPause)
            loudness+=Math.abs(samples[i]);
        loudness/=nofSamplesInWindow / gapBetweenSamplesWhenDetectingPause;
        return loudness < loudnessThreshold;
    }
}
