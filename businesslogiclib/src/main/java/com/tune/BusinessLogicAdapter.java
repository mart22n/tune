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
        frequencyExtractor = new FrequencyExtractor(audioRecordListener.sampleRate(), 30); //TODO: get this from settings
    }

    @Override
    public void update(Observable observable, Object data) {
        Pair<double[ ], Integer> pair = (Pair<double[ ], Integer>)data;
        double[] sample = pair.first;
        int sampleSize = pair.second;
        sample = harmonicsRemover.removeHarmonics(sample, sampleSize);
        frequencyExtractor.extractGroupsOfFrequencies(sample, sampleSize);
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
