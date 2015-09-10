package com.tune;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by mart22n on 8.09.2015.
 */
public class FrequencyExtractorTest {
    private FrequencyExtractor frequencyExtractor;
    @Before
    public void setUp() {
        frequencyExtractor = new FrequencyExtractor(AudioRecordListener.SAMPLE_RATE_STANDARD, 0);
    }

    // test method to add two values
    @Test
    public void getFreqFromFiveCrossingsGivesRoughlyCorrectFreq() {
        double[] pressureValues = { 0, 1, 2, 1,
                                    1, -1, -2, -1,
                                    -1, 1, 2, 1,
                                    1, -1, -2, -1,
                                    -1, 1, 2, 1,
                                    1, -1}; // diff between first and last crossing is 17 samples
        int sampleSize = 22;
        Assert.assertTrue(5512 == (int)frequencyExtractor.getFreqFromFiveCrossings(pressureValues, sampleSize));
    }

    @Test
    public void multipleZeroValuesInARowInInputGivesRoughlyCorrectFreq() {
        double[] pressureValues = {
                1, 1, -1, -2,
                -1, -1, 1, 2,
                1, 1, -1, 0,
                0, 0, 1, 0,
                0, 0, -1, 0}; // diff between first and last crossing is 16 samples
        int sampleSize = 20;
        Assert.assertTrue(5512 == (int) frequencyExtractor.getFreqFromFiveCrossings(pressureValues, sampleSize));
    }

    @Test
    public void ifTooQuiet_errorIsEmitted() {
        double[] pressureValues = {
                0, 1, 2, 1,
                1, -1, -2, -1,
                -1, 1, 2, 1,
                1, -1, -2, -1,
                -1, 1, 2, 1,
                1, -1}; // diff between first and last crossing is 17 samples
        int sampleSize = 22;
        frequencyExtractor = new FrequencyExtractor(AudioRecordListener.SAMPLE_RATE_STANDARD, 30);
        frequencyExtractor.getFreqFromFiveCrossings(pressureValues, sampleSize);
        // reset loudness threshold
        Assert.assertEquals(FrequencyExtractor.ReadingType.TOO_QUIET, frequencyExtractor.readingType());
        frequencyExtractor = new FrequencyExtractor(AudioRecordListener.SAMPLE_RATE_STANDARD, 0);
    }

    @Test
    public void ifLoudEnough_errorIsNotEmitted() {
        double[] pressureValues = {
                0, 1, 2, 1,
                1, -1, -2, -1,
                -1, 1, 2, 1,
                1, -1, -2, -1,
                -1, 1, 2, 1,
                1, -1}; // diff between first and last crossing is 17 samples
        int sampleSize = 22;
        frequencyExtractor = new FrequencyExtractor(AudioRecordListener.SAMPLE_RATE_STANDARD, 1);
        frequencyExtractor.getFreqFromFiveCrossings(pressureValues, sampleSize);
        // reset loudness threshold
        Assert.assertEquals(FrequencyExtractor.ReadingType.NO_PROBLEMS, frequencyExtractor.readingType());
        frequencyExtractor = new FrequencyExtractor(AudioRecordListener.SAMPLE_RATE_STANDARD, 0);
    }

    @Test
    public void onlyCrossingsWithSimilarWavelengthsAreTakenIntoAccount() {
        double[] pressureValues = {
                0, 1, 0, -1,
                0, 1, 0, -1,
                -1, -1, 0, 1,
                0, -1, 0, 1,
                0, -1, 0, 1
                }; // diff between first and last crossing is 8 samples
        int sampleSize = 20;
        Assert.assertTrue(11025 == (int) frequencyExtractor.getFreqFromFiveCrossings(pressureValues, sampleSize));
    }

    @Test
    public void ifNoFiveConsecutiveCrossingsWithSimilarWavelengthsAreFound_errorIsEmitted() {

    }

    @Test
    public void ifSampleLenIsLessThanMinimal_errorIsEmitted() {

    }

    @Test
    public void ifSampleLenIsBiggerOrEqualThanMinimal_errorIsNotEmitted() {

    }
}
