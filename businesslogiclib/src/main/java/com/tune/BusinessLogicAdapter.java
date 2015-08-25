package com.tune;

import android.util.Pair;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

class BusinessLogicAdapter implements Observer {
    public BusinessLogicAdapter(AudioRecordListener listener) {
        audioRecordListener = listener;
    }
    @Override
    public void update(Observable observable, Object data) {
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

    private AudioRecordListener audioRecordListener;
}
