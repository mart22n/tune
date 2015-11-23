package com.tune.businesslogic;

/**
 * Created by mart22n on 9.11.2015.
 */
public class TestBase {
    protected FrequencyExtractor frequencyExtractor;
    protected NoteAndDeviationIdentifier noteAndDeviationIdentifier;
    protected RollingAverageFinder rollingAverageFinder;
    protected void setFESettings(int sampleRate, int loudnessThreshold, int nofConsecutiveCrossingsToMeasure,
                               double measurementWindowMs, double maxDiffInPercent, int gapBetweenSamplesWhenDetectingPause) {
        FrequencyExtractor.FrequencyExtractorSettings s = new FrequencyExtractor.FrequencyExtractorSettings();
        s.sampleRate = sampleRate;
        s.loudnessThreshold = loudnessThreshold;
        s.maxDiffInPercent = maxDiffInPercent;
        s.measurementWindowMs = measurementWindowMs;
        s.setNofConsecutiveUpwardsCrossingsToMeasure(nofConsecutiveCrossingsToMeasure);
        s.gapBetweenSamplesWhenDetectingPause = gapBetweenSamplesWhenDetectingPause;
        frequencyExtractor = new FrequencyExtractor(s);
    }

    protected void setNISettings(int measurementWindowMs, int minNoteLenMs,
                               int octaveSpan, int deviationWhereBorderlineStarts) {
        NoteAndDeviationIdentifier.NoteIdentifierSettings s = new NoteAndDeviationIdentifier.NoteIdentifierSettings();
        s.measurementWindowMs = measurementWindowMs;
        s.minNoteLenMs = minNoteLenMs;
        s.octaveSpan = octaveSpan;
        s.deviationWhereBorderLineStarts = deviationWhereBorderlineStarts;
        noteAndDeviationIdentifier = new NoteAndDeviationIdentifier(s);
    }
}
