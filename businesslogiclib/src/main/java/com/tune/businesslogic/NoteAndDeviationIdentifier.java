package com.tune.businesslogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mart22n on 25.08.2015.
 * Zone means a frequency area around a note's frequency, where frequencies are considered as the
 * same note. It is +/- 45 cents around a note freq. The zone 45-50 cents around a note freq is
 * borderline zone, where note cannot be identified
 */
public class NoteAndDeviationIdentifier {

    public static class NoteIdentifierSettings {
        public int measurementWindowMs;
        public int minNoteLenMs;
        public double referenceFreq;
        public int octaveSpan; // this many octaves can a note be below or above reference freq to still be valid
        public int deviationWhereBorderLineStarts;
    }

    private int measurementWindowMs;
    private int minNoteLenMs;
    private double referenceFreq;
    private double[] freqsOfDegrees;
    private int octaveSpan;
    private Note lastPartialNote;
    private Note curNote;
    private int deviationWhereBorderLineStarts;

    NoteAndDeviationIdentifier(NoteIdentifierSettings settings) {
        this.measurementWindowMs = settings.measurementWindowMs;
        this.minNoteLenMs = settings.minNoteLenMs;
        this.referenceFreq = settings.referenceFreq;
        this.octaveSpan = settings.octaveSpan;
        this.deviationWhereBorderLineStarts = settings.deviationWhereBorderLineStarts;
        calculateDegreeFreqs();
        curNote = new Note();
    }

    Note[] convertWaveformToNotes(double[] inputWindows) {
        List<Note> notes = new ArrayList<>();
        int windowCountInNote = 0;
        // curTempNote is the note we are currently parsing; curNote is the note which may have
        // started somewhere in the past and curTempNote can be concatenated to curNote to form
        // a whole note. e.g. when we have window sequence
        // note1-note1-note1-shortpause-note1-note1-note1-note1, then curNote started at window0,
        // and continues at window4.
        Note curTempNote = new Note();
        int noteLen = 0;
        Note.NoteType curNoteType  = Note.NoteType.UNDEFINED;

        for(int i = 0; i < inputWindows.length; ++i) {
            curNoteType = getNoteTypeFromInput(inputWindows[i]);
            if(referenceFreq == -1 && curNoteType == Note.NoteType.VALIDNOTE) {
                referenceFreq = inputWindows[i];
            }
            ++noteLen;
            if (i > 0) {
                if (newNoteBegan(inputWindows, curNoteType, i)) {
                    curTempNote.length = (noteLen - 1) * measurementWindowMs;
                    if (curTempNoteLongEnough(noteLen - 1)) {
                        if (curTempNote.degree == curNote.degree && curTempNote.type == curNote.type) {
                            concatNotes(curNote, curTempNote);
                        }
                        else {
                            if(curNoteExists()) {
                                notes.add(curNote);
                            }
                            curNote = new Note(curTempNote);
                        }
                    }
                    if(i == inputWindows.length - 1) {
                        lastPartialNote = initializeNote(curNoteType, inputWindows[i]);
                    }
                    else {
                        curTempNote = initializeNote(curNoteType, inputWindows[i]);
                    }
                    noteLen = 1;
                }
                else if(i == inputWindows.length - 1) {
                    if(curTempNote.type == Note.NoteType.VALIDNOTE || curTempNote.type == Note.NoteType.BORDERLINE) {
                        curTempNote.addDeviation(calcDeviation(inputWindows[i],
                                curTempNote.degree), freqIsBorderline(inputWindows[i], curTempNote.degree));
                    }
                    curTempNote.length = noteLen * measurementWindowMs;
                    if (curTempNoteLongEnough(noteLen)) {
                        if (curTempNote.degree == curNote.degree && curTempNote.type == curNote.type) {
                            concatNotes(curNote, curTempNote);
                        }
                        else {
                            if(curNoteExists()) {
                                notes.add(curNote);
                            }
                            curNote = new Note(curTempNote);
                        }
                    }
                    else {
                        if(curNote.type != Note.NoteType.UNDEFINED) {
                            if (curTempNote.degree == curNote.degree && curTempNote.type == curNote.type) {
                                concatNotes(curNote, curTempNote);
                            }
                            else
                                lastPartialNote = new Note(curTempNote);
                        }
                        else {
                            curNote = new Note(curTempNote);
                        }
                    }
                }
                else {
                    curTempNote.length = noteLen * measurementWindowMs;
                    if(curTempNote.type == Note.NoteType.VALIDNOTE || curTempNote.type == Note.NoteType.BORDERLINE) {
                        curTempNote.addDeviation(calcDeviation(inputWindows[i],
                                curTempNote.degree), freqIsBorderline(inputWindows[i], curTempNote.degree));
                    }
                }
            }
            else {
                if(i == inputWindows.length - 1) {
                    curTempNote = initializeNote(curNoteType, inputWindows[0]);
                    if (curTempNote.degree == curNote.degree && curTempNote.type == curNote.type) {
                        concatNotes(curNote, curTempNote);
                    }
                }
                else {
                    curTempNote = initializeNote(curNoteType, inputWindows[0]);
                    if (lastPartialNote != null) {
                        if(lastPartialNote.type == curTempNote.type && lastPartialNote.degree == curTempNote.degree) {
                            noteLen += lastPartialNote.length / measurementWindowMs;
                            concatNotes(lastPartialNote, curTempNote);
                            curTempNote = new Note(lastPartialNote);
                        }
                        lastPartialNote = null;
                    }
                }
            }
        }
        Note[] ret = new Note[notes.size()];
        return notes.toArray(ret);
    }

    private Note initializeNote(Note.NoteType curNoteType, double freq) {
        Note ret;
        ret = new Note();
        ret.type = curNoteType;
        if(curNoteType == Note.NoteType.VALIDNOTE || curNoteType == Note.NoteType.BORDERLINE) {
            ret.degree = nearestDegreeToTheFreq(freq);
            ret.addDeviation(calcDeviation(freq,
                    ret.degree), freqIsBorderline(freq, ret.degree));
        }
        ret.length = measurementWindowMs;
        return ret;
    }

    private boolean curNoteExists() {
        return curNote.type != Note.NoteType.UNDEFINED;
    }

    private void concatNotes(Note note1, Note note2) {
        note1.length += note2.length;
        if(note2.type == Note.NoteType.VALIDNOTE || note2.type == Note.NoteType.BORDERLINE) {
            for (int j = 0; j < note2.length / measurementWindowMs; ++j) {
                note1.addDeviation(note2.getDeviation(j), note2.type == Note.NoteType.BORDERLINE);
            }
        }
    }

    private boolean curTempNoteLongEnough(int windowCountInNote) {
        return windowCountInNote >= minNoteLenMs / measurementWindowMs;
    }


    private boolean newNoteBegan(double[] inputWindows, Note.NoteType curNoteType, int i) {
        return getNoteTypeFromInput(inputWindows[i]) != curNoteType ||
                freqsAreDifferentNotes(inputWindows[i - 1], inputWindows[i]);
    }

    /**
     * when recording ends, this method is called
     * @return
     */
    Note lastPartialNote() {
            return lastPartialNote;
    }

    /**
     * only for testing
     * @return
     */
    Note curNote() {
        return curNote;
    }

    private Note.NoteType getNoteTypeFromInput(double freq) {
        Note.NoteType ret;
        if(freq == 0)
            ret = Note.NoteType.PAUSE;
        else if(freq == -1)
            ret = Note.NoteType.NOISE;
        else if(referenceFreq == -1)
            ret = Note.NoteType.VALIDNOTE;
        else if(freqFallsOutsideValidOctaveSpan(freq))
            ret = Note.NoteType.OUTOFRANGE;
        else {
            int degree = nearestDegreeToTheFreq(freq);
            if(freqIsBorderline(freq, degree)) {
                ret = Note.NoteType.BORDERLINE;
            }
            else {
                ret = Note.NoteType.VALIDNOTE;
            }
        }
        return ret;
    }

    private boolean freqsAreDifferentNotes(double freq1, double freq2) {
        return nearestDegreeToTheFreq(freq1) != nearestDegreeToTheFreq(freq2);
    }

    private int nearestDegreeToTheFreq(double freq) {
        int index = 0;
        for(int i = 0; i < 2 * 12 * octaveSpan; ++i) {
            if(freqsOfDegrees[i] >= freq) {
                index = i;
                break;
            }
        }
        if(Math.log(freqsOfDegrees[index] / freq) / Math.log(2) * 1200 > 50)
            return index - 1 - 12 * octaveSpan;
        return index - 12 * octaveSpan;
    }

    private void calculateDegreeFreqs() {
        freqsOfDegrees = new double[48];
        for(int i = -octaveSpan * 12; i < 0; ++i) {
            freqsOfDegrees[24 + i] = referenceFreq * Math.pow(2, (double)(i) / 12);
        }

        for(int i = 0; i < octaveSpan * 12; ++i) {
            freqsOfDegrees[i + octaveSpan * 12] = referenceFreq * Math.pow(2, (double)i / 12);
        }
    }

    private boolean freqFallsOutsideValidOctaveSpan(double freq) {
        if(freq < referenceFreq * Math.pow(2, -1 * ((double)octaveSpan)) ||
            (freq > referenceFreq * Math.pow(2, ((double)octaveSpan))))
            return true;
        return false;
    }

    private int calcDeviation(double freq, int nearestDegreeToTheFreq) {
        double degreeFreq =  Math.pow(2, ((double)nearestDegreeToTheFreq + 12 * octaveSpan - (double)(octaveSpan * 12)) / 12) * referenceFreq;
        return (int)(Math.log(freq / degreeFreq) / Math.log(2) * 1200);
    }

    private boolean freqIsBorderline(double freq, double degree) {
        double degreeFreq =  Math.pow(2, degree / 12) * referenceFreq;
        double borderlineUpperLimit = degreeFreq * Math.pow(2, (double)deviationWhereBorderLineStarts / 1200);
        double borderlineLowerLimit = degreeFreq * Math.pow(2, -(double)deviationWhereBorderLineStarts / 1200);
        if(freq >= borderlineUpperLimit || freq <= borderlineLowerLimit)
            return true;
        return false;
    }
}
