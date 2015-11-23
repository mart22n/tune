package com.tune.businesslogic;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by mart22n on 8.09.2015.
 */
public class FrequencyExtractorUnitTest extends TestBase {
    @Before
    public void setUp() {
        int loudnessThreshold = 0;
        int nofConsecutiveCrossingsToMeasure = 5;
        int maxDiffInPercent = 1;
        int  measurementWindowMs = 30;
        int gapBetweenSamplesWhenDetectingPause = 1;
        setFESettings(AudioRecordListener.SAMPLE_RATE_STANDARD, loudnessThreshold,
                nofConsecutiveCrossingsToMeasure, measurementWindowMs, maxDiffInPercent, gapBetweenSamplesWhenDetectingPause);
    }

    // test method to add two values
    @Test
    public void getFreqFromConsecutiveCrossingsGivesRoughlyCorrectFreq() {
        double[] samples =
                {0, 1, 2, 1, 1, -1, -2, -1,
                -1, 1, 2, 1, 1, -1, -2, -1,
                -1, 1, 2, 1, 1, -1, -2, -1,
                -1, 1, 2, 1, 1, -1, -2, -1};
        int sampleRate = 1000;
        int measurementWindowLenMs = 32;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);
        Assert.assertEquals(125, (int) frequencyExtractor.extractFrequencies(samples, samples, samples.length)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifMultipleZeroPressuresInARowInInput_roughlyCorrectFreqIsReturned() {
        double[] samples = {
                0, 1, 0, 0, 0, -1, 0, 0,
                0, 1, 0, 0, 0, -1, 0, 0,
                0, 1, 0, 0, 0, -1, 0, 0,
                0, 1, 0, 0, 0, -1, 0, 0
        };
        int sampleRate = 1000;
        int measurementWindowLenMs = 32;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);
        Assert.assertEquals(125, (int) frequencyExtractor.extractFrequencies(samples, samples, samples.length)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifPause_zeroFreqIsReturned() {
        double[] samples = {
                0, 1, 2, 1, 1, -1, -2, -1,
                -1, 1, 2, 1, 1, -1, -2, -1,
                -1, 1, 2, 1, 1, -1, -2, -1,
                -1, 1, 2, 1, 1, -1, -2, -1};

        int sampleRate = 1000;
        int measurementWindowLenMs = 32;
        int loudnessThreshold = 30;
        setFESettings(sampleRate, loudnessThreshold, 4, measurementWindowLenMs, 1, 1);
        Assert.assertEquals(0, (int) frequencyExtractor.extractFrequencies(samples, samples, samples.length)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifLoudEnough_correctFreqIsReturned() {
        double[] samples = {
                0, 2, 0, -2, 0, 2, 0, -2,
                0, 2, 0, -2, 0, 2, 0, -2};

        int loudnessThreshold = 1;
        int sampleRate = 1000;
        int measurementWindowLenMs = 16;
        setFESettings(sampleRate, loudnessThreshold, 4, measurementWindowLenMs, 1, 1);
        Assert.assertEquals(250, (int) frequencyExtractor.extractFrequencies(samples, samples, samples.length)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifAtFirstWeHaveCrossingsWithDifferentWavelengths_butAfterThatThereAreCrossingsWithSimilarWavelength_butTheyFallOutsideTheValidWindow_invalidFreqIsEmitted() {
        double[] samples = { // here we have 3 crossings with equal intervals, but 4th one is different.
                                    // After that we have 6 crossings with equal intervals, but the 6th one
                                    // is already outside the valid window
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, -1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1
        };
        int sampleRate = 1000;
        int measurementWindowLenMs = 36;
        int nofCrossings = 6;
        setFESettings(sampleRate, 0, nofCrossings, measurementWindowLenMs, 1, 1);
        Assert.assertEquals(-1, (int) frequencyExtractor.extractFrequencies(samples, samples, samples.length)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifInOneWindowWeHaveFirstlyNoise_andAfterThatValidFreq_invalidFreqIsReturned() {
        double[] samples = {
                0, 1, 0, -1, 0, -1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1
        };
        int sampleRate = 1000;
        int measurementWindowLenMs = 24;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);
        Assert.assertEquals(-1, (int) frequencyExtractor.extractFrequencies(samples, samples, samples.length)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    /**
     * In this test, we have less than required number of audible waves and a larger number of
     * too quiet waves
     */
/*    @Test
    public void ifLessThanMinimalAmountOfAudibleCrossingsWithinWindow_zeroFreqIsReturned() {
        int sampleRate = 1000;
        int measurementWindowLenMs = 16;
        int loudnessThreshold = 3;
        int nofCrossings = 6;

        setFESettings(sampleRate, loudnessThreshold, nofCrossings, measurementWindowLenMs, 1);
        double[] samples = {
                0, 4, -4, 4, -4, 4, -4, 4,
                -4, 4, -4, 1, -1, 1, -1, 1
        };
        Assert.assertEquals(0.0, frequencyExtractor.extractFrequencies(samples)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());

    }*/

    @Test
    public void ifInOneWindowWeHaveFirstlyF1_andAfterThatF2_F1IsReturned() {
        double[] samples = {
                0, 1, 1, 0, -1, -1, 0, 1,
                1, 0, -1, -1, 0, 1, 1, 0,
                 -1, -1, 0, 1, 1, // F1

                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1 // F2
        };

        int sampleRate = 1000;
        int measurementWindowLenMs = 37;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);
        Assert.assertEquals(166, (int) frequencyExtractor.extractFrequencies(samples, samples, samples.length)[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifPreviousInputHasAFullAndAPartialWindow_andNextInputHasTwoFullWindows_outputIs2CorrectFreqs() {
        int sampleRate = 1000;
        int measurementWindowLenMs = 50;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);
        double[] samples1 = {
                0, 1, 0, -1, 0, 1, 0, -1, // window1: 50 samples, 250Hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1,
                0, 1, 1, 0, -1, 0, 1, 1, // beginning of window2: 20 samples of 200 hz
                0, -1, 0, 1, 1, 0, -1, 0,
                1, 1, 0, -1};

        double[] res = frequencyExtractor.extractFrequencies(samples1, samples1, samples1.length);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(250.0, res[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());

        double[] samples2 = {
                0, 1, 1, 0,// end of window2: 30 samples of 200Hz
                -1, 0, 1, 1, 0, -1, 0, 1,
                1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1,
                0, -1,

                0, 1, 1, 1, 0, -1, -1, -1, // window3: 30 samples of 125Hz
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1
        };

        res = frequencyExtractor.extractFrequencies(samples2, samples2, samples2.length);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(200.0, res[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifPreviousInputHasAFullAndAPartialWindow_andNextInputHasAFullAndALongPartialWindow_outputIs3CorrectFreqs() {
        int sampleRate = 1000;
        int measurementWindowLenMs = 50;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);

        double[] samples = {
                0, 1, 1, 0, -1, -1,    // window 1: 50 samples of 166 Hz
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1,

                0, 1, 0, -1, 0, 1, 0, -1, // start of window2: 20 samples of 250Hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1
        };

        double[] res = frequencyExtractor.extractFrequencies(samples, samples, samples.length);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(166, (int) res[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());

        double[] samples2 = {
                0, 1, 0, -1, 0, 1, 0, -1,  // end of window2: 30 samples of 250Hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1,

                0, 1, 1, 0, -1, -1,    // window 3: 50 samples of 166 Hz
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1};

        res = frequencyExtractor.extractFrequencies(samples2, samples2, samples2.length);
        Assert.assertEquals(2, res.length);
        Assert.assertEquals(250.0, res[0]);
        Assert.assertEquals(166, (int)res[1]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

   @Test
   public void ifPrevInputIsLongAndLastInputHasOnlyPartialWindow_errorIsReturned() {
        int sampleRate = 1000;
        int measurementWindowLenMs = 50;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);

        double[] samples = {
                0, 1, 1, 0, -1, -1,    // window 1: 50 samples of 166 Hz
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1,

                0, 1, 1, 1, 0, -1, -1, -1, // start of window2: 20 samples of 125Hz
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1
        };
        double[] res = frequencyExtractor.extractFrequencies(samples, samples, samples.length);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(166, (int) res[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());

        double[] samples2 = {
                0, 1, 1, 1, 0, -1, -1, -1, // end of window2: 20 samples of 125Hz
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1
        };

        res = frequencyExtractor.extractFrequencies(samples2, samples2, samples2.length);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(-1.0, res[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.ONLY_SINGLE_PARTIAL_WINDOW_IN_INPUT, frequencyExtractor.readingType());
    }


    @Test
    public void ifMultipleMeasurementWindowsInInputAndLastWindowIsNotPartial_correctFreqsAreReturned() {
        double[] samples = {
                0, 1, 0, -1, 0, 1, 0, -1,// window1: 20 samples of 250Hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,// window2: 20 samples of 250Hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1
        };
        int sampleRate = 1000;
        int measurementWindowLenMs = 20;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);
        double[] res = frequencyExtractor.extractFrequencies(samples, samples, samples.length);
        Assert.assertEquals(2, res.length);
        Assert.assertEquals(250.0,res[0]);
        Assert.assertEquals(250.0,res[1]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }

    @Test
    public void ifWindowTooShort_errorIsReturned() {
        double[] samples = {
                0, 1, 1, 0, -1, -1,    // 166 Hz
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
                0, 1, 1, 0, -1, -1,
        };

        int sampleRate = 1000;
        int measurementWindowLenMs = 18;
        setFESettings(sampleRate, 0, 4, measurementWindowLenMs, 1, 1);
        double[] res = frequencyExtractor.extractFrequencies(samples, samples, samples.length);
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(-1.0,res[0]);
        Assert.assertEquals(FrequencyExtractor.ReadingType.OK, frequencyExtractor.readingType());
    }


}
