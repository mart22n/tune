package com.tune.businesslogic;

import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        s.loudnessThreshold = 30;
        s.maxDiffInPercent = 1;
        s.measurementWindowMs = 30;
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
            List<Double> samples = (List<Double>)data;
            int size = samples.size();
            if(size == 0) return;

            double[] samplesOrig = new double[size];
            for (int i = 0; i < size; ++i) {
                samplesOrig[i] = samples.get(i);
            }

            double[] samplesWithRemovedHarmonics = new double[size * 4 + 100];

            for (int i = 0; i < size; ++i) {
                samplesWithRemovedHarmonics[i] = samples.get(i);
            }

            harmonicsRemover.removeHarmonics(samplesWithRemovedHarmonics, size);
            //writeSamplesToFile(samplesWithRemovedHarmonics, size);

            double[] freqs = frequencyExtractor.extractFrequencies(samplesWithRemovedHarmonics, samplesOrig, size);

            Note[] notes = noteAndDeviationIdentifier.convertWaveformToNotes(freqs);
            blaListener.onNotesOrPausesAvailable(notes, noteAndDeviationIdentifier.noteChanged());
            //Log.d(tag, "Count = " + String.valueOf(notes.length) + ":");
            for (int i = 0; i < notes.length; ++i) {
/*                Log.d(tag, "Note len = " + String.valueOf(notes[i].lengthMs) + "; Degree = " + String.valueOf(notes[i].degree) +
                "; Type = " + String.valueOf(notes[i].type));*/
            }
        }
    }

    private void writeSamplesToFile(double[] samples, int size) {
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File("/storage/emulated/0/Android/data/com.tune/files", "audiosamples.txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, true);
        }
        catch (IOException ex) {
            Log.e(tag, "writeSamplesToFile(): cannot create fileOutputStream");
        }
        try {
            //writeToFileAsHumanReadable(samples, size, fileOutputStream);
            writeToFileAsBinary(samples, size, fileOutputStream);

        }
        catch(IOException io) {
            Log.e(tag, "writeSamplesToFile(): cannot write to fileOutputStream");
        }

    }

    private void writeToFileAsBinary(double[] samples, int size, FileOutputStream fileOutputStream) throws IOException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream d = new java.io.DataOutputStream(b);
        for(int i = 0; i < size; ++i) {
            d.writeDouble(samples[i]);
        }
        d.flush();
        fileOutputStream.write(b.toByteArray(), 0, b.size());
        fileOutputStream.close();
    }
    private void writeToFileAsHumanReadable(double[] samples, int size, FileOutputStream fileOutputStream) throws IOException {
        byte[] fileContents = new byte[size * 30];
        int indexInFileContents = 0;
        for (int i = 0; i < size; ++i) {//TODO: i+= 1
            double d = samples[i];
            byte[] tmp = ((Double) d).toString().concat("\\r\\n").getBytes();
            System.arraycopy(tmp, 0, fileContents, indexInFileContents, tmp.length);
            indexInFileContents += tmp.length;
        }
        fileOutputStream.write(fileContents, 0, indexInFileContents);
        fileOutputStream.close();
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
