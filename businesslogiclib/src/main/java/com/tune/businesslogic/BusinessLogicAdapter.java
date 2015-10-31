package com.tune.businesslogic;

import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class BusinessLogicAdapter extends Observable implements Observer  {
    private AudioRecordListener audioRecordListener;
    private FrequencyExtractor frequencyExtractor;
    private NoteAndDeviationIdentifier noteAndDeviationIdentifier;
    private BusinessLogicAdapterListener blaListener;
    private String tag = "tune";

    private HarmonicsRemover harmonicsRemover;
    public BusinessLogicAdapter(AudioRecordListener listener, BusinessLogicAdapterListener blaListener) {
        harmonicsRemover = new HarmonicsRemover();
        audioRecordListener = listener;
        audioRecordListener.addObserver(this);
        FrequencyExtractor.FrequencyExtractorSettings s = new FrequencyExtractor.FrequencyExtractorSettings(); //TODO: get this from settings
        s.sampleRate = AudioRecordListener.SAMPLE_RATE_STANDARD;;
        s.loudnessThreshold = 0;
        s.maxDiffInPercent = 1;
        s.measurementWindowMs = 100;
        s.nofConsecutiveUpwardsCrossingsToMeasure = 4;
        s.gapBetweenSamplesWhenDetectingPause = 100;
        frequencyExtractor = new FrequencyExtractor(s);
        frequencyExtractor.addObserver(this);

        NoteAndDeviationIdentifier.NoteIdentifierSettings noteIdentifierSettings = new NoteAndDeviationIdentifier.NoteIdentifierSettings();
        noteIdentifierSettings.deviationWhereBorderLineStarts = 40;
        noteIdentifierSettings.measurementWindowMs = (int)s.measurementWindowMs;
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
            double[] samples = ((Pair<double[], Integer>)(data)).first;
            int size = ((Pair<double[], Integer>)(data)).second;
            double[] origSamples = new double[size];
            System.arraycopy(samples, 0, origSamples, 0, size);
            //harmonicsRemover.removeHarmonics(samples, size); //TODO: uncomment
            //writeSamplesToFile(samples, size);
            double[] samplesTrimmed = new double[size];
            System.arraycopy(samples, 0, samplesTrimmed, 0, size);

            double[] freqs = frequencyExtractor.extractFrequencies(samplesTrimmed, origSamples, size);
            Log.d(tag, "freqs = ");
            for(int i = 0; i < freqs.length; ++i) {
                Log.d(tag, String.valueOf(freqs[i]));
            }
            Note[] notes = noteAndDeviationIdentifier.convertWaveformToNotes(freqs);
            blaListener.onNewNotesOrPausesAvailable(notes);
            Log.d(tag, "Notes.size = " + notes.length);
            // control goes to FE -> SPF->VD->NE->DF->NI
            //blaListener.onFirstNoteDetected(new Note());
        }
    }

    private void writeSamplesToFile(double[] samples, int size) {
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File("/storage/emulated/0/Android/data/com.tune/files", "audiosamples.txt");
            fileOutputStream = new FileOutputStream(file, true);
        }
        catch (IOException ex) {
            Log.e(tag, "writeSamplesToFile(): cannot create fileOutputStream");
        }
        try {
            for (int i = 0; i < size; ++i) {
                byte[] tmp = new byte[20];
                double d = samples[i];
                fileOutputStream.write(((Double) d).toString().getBytes(), 0, ((Double) d).toString().getBytes().length);
                byte[] newLine = new byte[] { 0x0D, 0x0A };
                fileOutputStream.write(newLine, 0, 2);
            }
            fileOutputStream.close();
        }
        catch(IOException io) {
            Log.e(tag, "writeSamplesToFile(): cannot write to fileOutputStream");
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
