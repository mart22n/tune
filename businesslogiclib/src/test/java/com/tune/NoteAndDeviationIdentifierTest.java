package com.tune;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by mart22n on 15.09.2015.
 */
public class NoteAndDeviationIdentifierTest {
    private NoteAndDeviationIdentifier uut;

    @Before
    public void setUp() {
        setNISettings(30, 100, 440, 2, 45);
    }

    @Test
    public void whenMultipleZeroFreqWindowsInARow_andAfterThatANote_pauseAndCorrectLastNoteIsReturned() {
        double[] input = new double[] { 0, 0, 0, 0, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(Note.NoteType.PAUSE, uut.curNote().type);
        Assert.assertEquals(120, uut.curNote().length);
        Assert.assertEquals(-12, uut.lastPartialNote().degree);
        Assert.assertEquals(30, uut.lastPartialNote().length);
    }

    @Test
    public void whenMultipleValidFreqWindowsInARow_andAfterThatAPause_noteAndCorrectLastNoteIsReturned() {
        double[] input = new double[] { 220, 220, 220, 220, 0 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, uut.curNote().type);
        Assert.assertEquals(120, uut.curNote().length);
        Assert.assertEquals(Note.NoteType.PAUSE, uut.lastPartialNote().type);
        Assert.assertEquals(30, uut.lastPartialNote().length);
    }

    @Test
    public void whenWeHaveALongSingleNote_outputIsCorrect() {
        double[] input = new double[] { 220, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(120, uut.curNote().length);;
    }

    @Test
    public void whenWeHaveAShortNoteAndThenALongSingleNote_outputIsCorrect() {
        double[] input = new double[] { 0, 220, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(120, uut.curNote().length);;
    }

    @Test
    public void whenFreqFalls20CentsBelowDegree_correctDeviationAndDegreeIsReturned() {
        double[] input = new double[] { 220, 220, 220, 217.47, 217.47, 217.47 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(-12, uut.curNote().degree);
        Assert.assertEquals(-20, (int) uut.curNote().getDeviation(3));
    }

    @Test
    public void whenFreqFalls60CentsBelowDegree_correctDeviationAndDegreeIsReturned() {
        double[] input = new double[] { 220, 220, 220, 212.51, 212.51, 212.51 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(-12, notes[0].degree);
        Assert.assertEquals(-13, uut.curNote().degree);
        Assert.assertEquals(40, (int)uut.curNote().getDeviation(0));
    }

    @Test
    public void whenTwoNoteFreqsWithTransitionAreInputted_singleNoteIsReturned() {
        double[] input = new double[] { 220, 220, 220, 260, 260, 300, 300, 300 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(-12, notes[0].degree);
        Assert.assertEquals(90, uut.curNote().length);
    }

    @Test
    public void whenTwoIdenticalFreqsWithLittleEnoughNoiseInBetweenAreInputted_noNoteReturned() {
        double[] input = new double[] { 220, 220, 220, -1, -1, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(180, uut.curNote().length);
    }

    @Test
    public void whenTwoIdenticalFreqsWithLongEnoughNoiseInBetweenAreInputted_twoNotesAreReturned() {
        double[] input = new double[] { 220, 220, 220, -1, -1, -1, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(90, notes[0].length);
        Assert.assertEquals(90, notes[1].length);
        Assert.assertEquals(Note.NoteType.NOISE, notes[1].type);
    }

    @Test
    public void whenTwoDifferentFreqsWithShortNoiseInBetweenAreInputted_firstNoteIsReturned() {
        double[] input = new double[] { 220, 220, 220, -1, -1, 440, 440, 440 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(90, notes[0].length);
        Assert.assertEquals(-12, notes[0].degree);
    }

    @Test
    public void whenUpperBorderlineFreqIsInputted_noteWithTypeBorderlineIsReturned() {
        int borderlineDeviationInCents = 45;
        setNISettings(30, 100, 440, 2, borderlineDeviationInCents);
        double[] input = new double[] { 226.05, 226.05, 226.05 };
        uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.BORDERLINE, uut.curNote().type);
        Assert.assertEquals(-12, uut.curNote().degree);
    }

    @Test
    public void whenLowerBorderlineFreqIsInputted_noteWithTypeBorderlineIsReturned() {
        int borderlineDeviationInCents = 45;
        setNISettings(30, 100, 440, 2, borderlineDeviationInCents);
        double[] input = new double[] { 214.11, 214.11, 214.11 };
        uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.BORDERLINE, uut.curNote().type);
        Assert.assertEquals(-12, uut.curNote().degree);
    }

    @Test
    public void whenOneOctaveHigherFreqThanReferenceFreqIsInputted_correctNoteIsReturned() {
        double refFreq = 440;
        setNISettings(30, 100, refFreq, 2, 45);
        double[] input = new double[] { 880, 880, 880 };
        uut.convertWaveformToNotes(input);
        Assert.assertEquals(12, uut.curNote().degree);
    }


    @Test
    public void whenFreqIsOutsideValidRange_correctNoteTypeIsReturned() {
        double refFreq = 440;
        int octaveSpan = 2;
        setNISettings(30, 100, refFreq, octaveSpan, 45);
        double[] input = new double[] { 2000, 2000, 2000 };
        uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.OUTOFRANGE, uut.curNote().type);
    }

    @Test
    public void whenNoteNearBorderAndThenShortNoiseAndThenNoteOnTheOtherSideOfBorderNearBorder_singleNoteIsReturned() {
        double[] input = new double[] { 214.98, 214.98, 214.98, -1, -1, 212.51, 212.51, 212.51, 0 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(-12, notes[0].degree);
        Assert.assertEquals(-13, uut.curNote().degree);
        Assert.assertEquals(-39, (int) notes[0].getDeviation(0));
        Assert.assertEquals(40, (int)uut.curNote().getDeviation(0));
    }

    @Test
    public void whenWeHaveLongNoteSequenceInInput_correctNotesAreReturned() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 220, 220, 440, 440, 440 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(Note.NoteType.PAUSE, notes[0].type);
        Assert.assertEquals(90, notes[0].length);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[1].type);
        Assert.assertEquals(120, notes[1].length);
        Assert.assertEquals(0, uut.curNote().degree);
        Assert.assertEquals(90, uut.curNote().length);
    }

    @Test
    public void ifAtFirstWeHaveOneWindowOfPause_andAfterThatLongEnoughNote_outputIsCorrect() {
        double[] input = new double[] { 0, 222, 222, 222, 222 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(-12, uut.curNote().degree);
        Assert.assertEquals(15, (int) uut.curNote().getDeviation(0));
    }

    @Test
    public void ifValidNoteCrossesBorderByChanging20Cents_newNoteIsReturned() {
        double[] input = new double[] { 225.14, 225.14, 225.14, 227.76, 227.76, 227.76, 0 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(-12, notes[0].degree);
        Assert.assertEquals(-11, uut.curNote().degree);
    }

    @Test
    public void ifWeHaveAtFirstLongPause_andThenShortNote_andThenAgainLongPause_andThenShortNote_singlePauseIsReturned() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 0, 0, 0, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(180, uut.curNote().length);
    }

    @Test
    public void ifWeHaveSequenceOfLongNotesAndShortPausesInBetween_singleNoteIsReturned() {
        double[] input = new double[] { 220, 220, 220, 0, 0, 220, 220, 220, 0, 0, 220, 220, 220, 0 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(270, uut.curNote().length);
    }

    @Test
    public void testMultipleInputSequence1() {
        double[] input = new double[] { 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 220, 220, 220};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(180, uut.curNote().length);
    }

    @Test
    public void testMultipleInputSequence2() {
        double[] input = new double[] { 220, 220, 220, 440, 440, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 440, 440, 440};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(90, notes[0].length);
        Assert.assertEquals(-12, notes[0].degree);
        Assert.assertEquals(90, uut.curNote().length);
    }

    @Test
    public void testMultipleInputSequence3() {
        double[] input = new double[] { 220, 220, 220, 440, 440, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 440, 440};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(90, uut.curNote().length);
        Assert.assertEquals(-12, uut.curNote().degree);
    }

    @Test
    public void testMultipleInputSequence5() {
        double[] input = new double[] { 120, 120, 120 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 440, 440};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(90, uut.curNote().length);

        input = new double[] { 120, 120, 120};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(180, uut.curNote().length);
    }


    @Test
    public void testMultipleInputSequence6() {
        double[] input = new double[] { 220, 220, 220, 440 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 440, 440, 220};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(-12, notes[0].degree);
        Assert.assertEquals(90, uut.curNote().length);
        Assert.assertEquals(0, uut.curNote().degree);
    }

    @Test
    public void testMultipleInputSequence7() {
        double[] input = new double[] { 220, 220, 220, 440 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 440, 220, 220, 440};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(90, uut.curNote().length);
        Assert.assertEquals(-12, uut.curNote().degree);
    }

    @Test
    public void testMultipleInputSequence8() {
        double[] input = new double[] { 220, 220, 220, 440 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 440, 220, 220, 220 };
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(180, uut.curNote().length);

        input = new double[] { 220, 220, 220 };
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(270, uut.curNote().length);
    }

    @Test
    public void testMultipleInputSequence9() {
        double[] input = new double[] { 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 220 };
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(120, uut.curNote().length);
    }

    @Test
    public void testMultipleInputSequence11() {
        double[] input = new double[] { 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);

        input = new double[] { 220, 220 };
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(150, uut.curNote().length);
    }

    @Test
    public void testMultipleInputSequence12() {
        double[] input = new double[] {220, 220};
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(60, uut.curNote().length);

        input = new double[] {220, 220, 220};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
        Assert.assertEquals(150, uut.curNote().length);
    }

    @Test
    public void ifNoteHasFirstDeviationOKButSecondIsBorderline_noteIsBorderline() {
        double[] input = new double[] {220, 226};
        uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.BORDERLINE, uut.curNote().type);
    }

    public void setNISettings(int measurementWindowMs, int minNoteLenMs, double referenceFreq,
                              int octaveSpan, int deviationWhereBorderlineStarts) {
        NoteAndDeviationIdentifier.NoteIdentifierSettings s = new NoteAndDeviationIdentifier.NoteIdentifierSettings();
        s.measurementWindowMs = measurementWindowMs;
        s.minNoteLenMs = minNoteLenMs;
        s.referenceFreq = referenceFreq;
        s.octaveSpan = octaveSpan;
        s.deviationWhereBorderLineStarts = deviationWhereBorderlineStarts;
        uut = new NoteAndDeviationIdentifier(s);
    }
}
