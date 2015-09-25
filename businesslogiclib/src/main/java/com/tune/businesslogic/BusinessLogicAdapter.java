package com.tune.businesslogic;

import android.util.Pair;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class BusinessLogicAdapter extends Observable implements Observer  {
    private AudioRecordListener audioRecordListener;
    private FrequencyExtractor frequencyExtractor;
    private NoteAndDeviationIdentifier noteAndDeviationIdentifier;
    private BusinessLogicAdapterListener blaListener;

    private HarmonicsRemover harmonicsRemover;
    public BusinessLogicAdapter(AudioRecordListener listener, BusinessLogicAdapterListener blaListener) {
        harmonicsRemover = new HarmonicsRemover();
        audioRecordListener = listener;
        FrequencyExtractor.FrequencyExtractorSettings s = new FrequencyExtractor.FrequencyExtractorSettings(); //TODO: get this from settings
        s.loudnessThreshold = 0;
        s.maxDiffInPercent = 1;
        s.measurementWindowMs = 0.03;
        s.nofConsecutiveUpwardsCrossingsToMeasure = 5;
        s.sampleRate = AudioRecordListener.SAMPLE_RATE_STANDARD;
        frequencyExtractor = new FrequencyExtractor(s);
        frequencyExtractor.addObserver(this);
        NoteAndDeviationIdentifier.NoteIdentifierSettings noteIdentifierSettings = new NoteAndDeviationIdentifier.NoteIdentifierSettings();
        noteIdentifierSettings.deviationWhereBorderLineStarts = 40;
        noteIdentifierSettings.measurementWindowMs = 30;
        noteIdentifierSettings.minNoteLenMs = 100;
        noteIdentifierSettings.octaveSpan = 2;
        noteAndDeviationIdentifier = new NoteAndDeviationIdentifier(noteIdentifierSettings);
        this.blaListener = blaListener;
    }

    @Override
    public void update(Observable observable, Object data) {
        if(data instanceof String) {
            blaListener.onToastNotification(data.toString());
        }
        else {
            double[] samples = (double[]) data;
            samples = harmonicsRemover.removeHarmonics(samples, samples.length);
            double[] freqs = frequencyExtractor.extractFrequencies(samples);
            Note[] notes = noteAndDeviationIdentifier.convertWaveformToNotes(freqs);
            blaListener.onNewNotesOrPausesAvailable(notes);

            // control goes to FE -> SPF->VD->NE->DF->NI
            //blaListener.onFirstNoteDetected(new Note());
        }
    }

    public void startListeningFirstNote() {
        throw new UnsupportedOperationException();
    }

    public void startListeningTune() {
        audioRecordListener.start();
    }

    public void stopListening() {
        audioRecordListener.stop();
    }

    public void setFrequencyExtractorOptions(FrequencyExtractor.FrequencyExtractorSettings s){
        frequencyExtractor = new FrequencyExtractor(s);
    }

    public void setNoteIdentifierOptions(NoteAndDeviationIdentifier.NoteIdentifierSettings s){
        noteAndDeviationIdentifier = new NoteAndDeviationIdentifier(s);
    }

    public List<Pair<Short, Short> > trendLineOfLastDeviations(int howMany){
        throw new UnsupportedOperationException();
    }
}
