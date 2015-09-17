package com.tune;

import android.util.Pair;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

class BusinessLogicAdapter implements Observer {
    private AudioRecordListener audioRecordListener;
    private FrequencyExtractor frequencyExtractor;

    private HarmonicsRemover harmonicsRemover;
    public BusinessLogicAdapter(AudioRecordListener listener) {
        harmonicsRemover = new HarmonicsRemover();
        audioRecordListener = listener;
        FrequencyExtractor.FrequencyExtractorSettings s = new FrequencyExtractor.FrequencyExtractorSettings(); //TODO: get this from settings
        s.loudnessThreshold = 0;
        s.maxDiffInPercent = 1;
        s.measurementWindowMs = 0.03;
        s.nofConsecutiveUpwardsCrossingsToMeasure = 5;
        s.sampleRate = AudioRecordListener.SAMPLE_RATE_STANDARD;
        frequencyExtractor = new FrequencyExtractor(s);
    }

    @Override
    public void update(Observable observable, Object data) {
        double[] samples = (double[])data;
        samples = harmonicsRemover.removeHarmonics(samples, samples.length);
        frequencyExtractor.extractFrequencies(samples);
        // control goes to FE -> SPF->VD->NE->DF->NI
        throw new UnsupportedOperationException();
    }

    public void startListeningFirstNote() {
        throw new UnsupportedOperationException();
    }

    public void startListeningTune() {
        throw new UnsupportedOperationException();
    }

    public void stopListening() {
        throw new UnsupportedOperationException();
    }

    public List<Pair<Short, Short> > trendLineOfLastDeviations(int howMany){
        throw new UnsupportedOperationException();
    }
}
