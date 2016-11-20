package com.tune.businesslogic;

import android.media.audiofx.NoiseSuppressor;

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
        setNISettings(30, 90, 2, 45);
    }

    @Test
    public void whenLongEnoughNoteIsFollowedByGlitch_glitchIsMergedIntoNote() {
        double[] input = new double[] { 220, 220, 220, 0 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(120, notes[0].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[0].type);
    }

    @Test
    public void whenLongEnoughNoteIsFollowedByGlitch_glitchIsMergedIntoNote2() {
        double[] input = new double[] { 220, 220, 220, 0, 0 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(150, notes[0].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[0].type);
    }

    /*@Test
    public void whenNote_andAfterThatAGlitch_glitchIsConcatenatedToNote() {
        double[] input = new double[] { 220, 220, 220, 0, 0, 0, 0, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(150, notes[1].lengthMs);
        Assert.assertEquals(Note.NoteType.PAUSE, notes[1].type);
    }

    @Test
    public void whenNoteInFirstWindow_andInSecondWindowThereIsANoteAndAfterThatGlitch_glitchIsConcatenatedToNote() {
        double[] input = new double[] { 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);

        input = new double[] { 220, 220, 220, 0, 0, 0, 0, 220 };
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(180, notes[0].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[0].type);
        Assert.assertEquals(150, notes[1].lengthMs);
        Assert.assertEquals(Note.NoteType.PAUSE, notes[1].type);
    }

    @Test
    public void whenWeHaveALongSingleNote_oneNoteIsReturned() {
        double[] input = new double[] { 220, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(120, notes[0].lengthMs);
    }

    @Test
    public void whenWeHaveAGlitchAndThenALongSingleNote_glitchIsPrependedToNote() {
        double[] input = new double[] { 0, 220, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(150, notes[0].lengthMs);
    }

    @Test
    public void whenFreqFalls20CentsBelowDegree_correctDeviationAndDegreeIsReturned() {
        double[] input = new double[] { 220, 220, 220, 217.47, 217.47, 217.47 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(180, notes[0].lengthMs);
        Assert.assertEquals(0, notes[0].degree);
        Assert.assertEquals(-20, notes[0].getDeviation(3));
    }

    @Test
    public void whenFreqFalls60CentsBelowDegree_correctDeviationAndDegreeIsReturned() {
        double[] input = new double[] { 220, 220, 220, 212.51, 212.51, 212.51 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(0, notes[0].degree);
        Assert.assertEquals(-1, notes[1].degree);
        Assert.assertEquals(40, notes[1].getDeviation(0));
    }

    @Test
    public void whenTwoNoteFreqsWithTransitionAreInputted_transitionIsConcatenatedToFirstNote() {
        double[] input = new double[] { 220, 220, 220, 260, 260, 300, 300, 300 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(0, notes[0].degree);
        Assert.assertEquals(150, notes[0].lengthMs);
        Assert.assertEquals(90, notes[1].lengthMs);

    }

    @Test
    public void whenTwoIdenticalFreqsWithGlitchInBetweenAreInputted_glitchIsBlendedIntoNote() {
        double[] input = new double[] { 220, 220, 220, -1, -1, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(240, notes[0].lengthMs);
    }

    @Test
    public void testLongerSeqWithNoise() {
        double[] input = new double[] { 220, 220, 220, 0, 0, 220, 220, 220, 0, 150, 100, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(3, notes.length);
        Assert.assertEquals(240, notes[0].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[0].type);
        Assert.assertEquals(8, notes[0].deviations.size());
        Assert.assertEquals(90, notes[1].lengthMs);
        Assert.assertEquals(Note.NoteType.NOISE, notes[1].type);
        Assert.assertEquals(90, notes[2].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[2].type);
    }

    @Test
    public void whenThereIsNoiseContainingSameFreqAsSurroundingNote_noiseIsMergedIntoSurroundingNote() {
        double[] input = new double[] { 220, 220, 220, 0, 220, 0, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(270, notes[0].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[0].type);
    }

    @Test
    public void whenTwoIdenticalFreqsWithLongEnoughNoiseInBetweenAreInputted_twoNotesAndNoiseAreReturned() {
        double[] input = new double[] { 220, 220, 220, -1, -1, -1, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(3, notes.length);
        Assert.assertEquals(90, notes[0].lengthMs);
        Assert.assertEquals(90, notes[1].lengthMs);
        Assert.assertEquals(90, notes[2].lengthMs);
        Assert.assertEquals(Note.NoteType.NOISE, notes[1].type);
    }

    @Test
    public void whenTwoDifferentFreqsWithGlitchInBetweenAreInputted_bothNotesAreReturned() {
        double[] input = new double[] { 220, 220, 220, -1, -1, 440, 440, 440 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(150, notes[0].lengthMs);
        Assert.assertEquals(0, notes[0].degree);
        Assert.assertEquals(90, notes[1].lengthMs);
    }

    @Test
    public void whenUpperBorderlineFreqIsInputted_noteWithTypeBorderlineIsReturned() {
        int borderlineDeviationInCents = 45;
        setNISettings(30, 90, 2, borderlineDeviationInCents);
        double[] input = new double[] { 220, 220, 220, 226.05, 226.05, 226.05 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.BORDERLINE, notes[0].type);
        Assert.assertEquals(0, notes[0].degree);
    }

    @Test
    public void whenLowerBorderlineFreqIsInputted_noteWithTypeBorderlineIsReturned() {
        int borderlineDeviationInCents = 45;
        setNISettings(30, 90, 2, borderlineDeviationInCents);
        double[] input = new double[] { 220, 220, 220, 214.11, 214.11, 214.11 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.BORDERLINE, notes[0].type);
        Assert.assertEquals(0, notes[0].degree);
    }

    @Test
    public void whenOneOctaveHigherFreqThanReferenceFreqIsInputted_correctNoteIsReturned() {
        setNISettings(30, 90, 2, 45);
        double[] input = new double[] { 440, 440, 440, 880, 880, 880 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(12, notes[1].degree);
    }


    @Test
    public void whenFreqIsOutsideValidRange_correctNoteTypeIsReturned() {
        int octaveSpan = 2;
        setNISettings(30, 90, octaveSpan, 45);
        double[] input = new double[] { 440, 440, 440, 2000, 2000, 2000 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.OUTOFRANGE, notes[1].type);
    }

    @Test
    public void whenNoteNearBorderAndThenShortNoiseAndThenNoteOnTheOtherSideOfBorderNearBorder_correctNotesReturned() {
        double[] input = new double[] { 440, 440, 440, 214.98, 214.98, 214.98, -1, -1, 212.51, 212.51, 212.51, 0 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(3, notes.length);
        Assert.assertEquals(0, notes[0].degree);
        Assert.assertEquals(-39, notes[1].getDeviation(0));
        Assert.assertEquals(150, notes[1].lengthMs);
        Assert.assertEquals(40, notes[2].getDeviation(0));
        Assert.assertEquals(-13, notes[2].degree);
    }

    @Test
    public void whenWeHaveLongNoteSequenceInInput_correctNotesAreReturned() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 220, 220, 440, 440, 440 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(3, notes.length);
        Assert.assertEquals(Note.NoteType.PAUSE, notes[0].type);
        Assert.assertEquals(90, notes[0].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[1].type);
        Assert.assertEquals(120, notes[1].lengthMs);
        Assert.assertEquals(12, notes[2].degree);
        Assert.assertEquals(90, notes[2].lengthMs);
    }

    @Test
    public void ifAtFirstWeHaveGlitch_andAfterThatLongEnoughNote_twoNotesAreReturned() {
        double[] input = new double[] { 0, 440, 440, 440, 222, 222, 222, 222 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(120, notes[0].lengthMs);
        Assert.assertEquals(-12, notes[1].degree);
        Assert.assertEquals(120, notes[1].lengthMs);
        Assert.assertEquals(15, (int) notes[1].getDeviation(0));
    }

    @Test
    public void ifValidNoteCrossesBorderByChanging20Cents_newNoteIsReturned() {
        double[] input = new double[] { 440, 440, 440, 225.14, 225.14, 225.14, 227.76, 227.76, 227.76 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(3, notes.length);
        Assert.assertEquals(-12, notes[1].degree);
        Assert.assertEquals(-11, notes[2].degree);
    }

    @Test
    public void ifWeHaveNoteIntersectingWithGlitches_glitchesAreMergedIntoNote() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 0, 0, 0, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(270, notes[0].lengthMs);
    }

    @Test
    public void ifWeHaveAtBeginningOfRecordingOnlyPausesOrNoises_noNotesAreReturned() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 0, 0, 0, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
    }

    @Test
    public void ifWeHaveNoiseAndThenANoteAndThenNoise_threeNotesAreReturned() {
        double[] input = new double[] { 30, 60, 85, 220, 220, 220, 30, 60, 85 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(3, notes.length);
    }

    @Test
    public void ifWeHavePauseAndThenTooShortNoteAndThenPauseAndThenTooShortNote_noNotesAreReturned() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 0, 0, 0, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(0, notes.length);
    }

    @Test
    public void ifWeHaveNoteIntersectingWithGlitches_glitchesAreMergedIntoNote_2() {
        double[] input = new double[] { 220, 220, 220, 0, 0, 220, 220, 220, 0, 0, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(390, notes[0].lengthMs);
    }

    @Test
    public void testSequence1() {
        double[] input = new double[] { 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
    }

    @Test
    public void testSequence2() {
        double[] input = new double[] { 220, 220, 220, 440, 440, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(180, notes[0].lengthMs);
    }

    @Test
    public void testSequence3() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 0, 0, 0, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(240, notes[0].lengthMs);
        Assert.assertEquals(90, notes[1].lengthMs);
    }

    @Test
    public void testSequence4() {
        double[] input = new double[] { 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);

        input = new double[] { 0, 0, 0, 220, 220, 0, 0, 0, 220, 110, 50 };
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
        Assert.assertEquals(240, notes[0].lengthMs);
        Assert.assertEquals(90, notes[1].lengthMs);
        Assert.assertEquals(Note.NoteType.NOISE, notes[1].type);
    }

    @Test
    public void testSequence5() {
        double[] input = new double[] { 0, 0, 0, 220, 220, 0, 0, 0, 220, 110, 180, 180, 180, 220, 110, 50 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(3, notes.length);
        Assert.assertEquals(240, notes[0].lengthMs);
        Assert.assertEquals(150, notes[1].lengthMs);
        Assert.assertEquals(Note.NoteType.VALIDNOTE, notes[1].type);
        Assert.assertEquals(90, notes[2].lengthMs);
        Assert.assertEquals(Note.NoteType.NOISE, notes[2].type);
    }

   @Test
    public void ifWeHaveMultipleGlithesInARow_theyAreInterpretedAsNoise() {
        double[] input = new double[] {123, 234, 345, 235, 220, 220, 220 };
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(2, notes.length);
       Assert.assertEquals(120, notes[0].lengthMs);
       Assert.assertEquals(Note.NoteType.NOISE, notes[0].type);
       Assert.assertEquals(90, notes[1].lengthMs);
    }


    @Test
    public void ifNoteHasFirstDeviationOKButSecondIsBorderline_noteIsBorderline() {
        double[] input = new double[] {220, 220, 220, 226};
        Note[] notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(Note.NoteType.BORDERLINE, notes[0].type);
    }

    @Test
    public void whenNoteChanged_noteChangedReturnsTrue() {
        double[] input = new double[] {220, 220, 220, 220};
        Note[] notes = uut.convertWaveformToNotes(input);

        Assert.assertEquals(true, uut.noteChanged());
        input = new double[] {320, 320, 320, 320};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(true, uut.noteChanged());
    }

    @Test
    public void whenNoteHasntChanged_noteChangedReturnsFalse() {
        double[] input = new double[] {220, 220, 220, 220};
        Note[] notes = uut.convertWaveformToNotes(input);

        Assert.assertEquals(true, uut.noteChanged());
        input = new double[] {220, 220, 220, 220};
        notes = uut.convertWaveformToNotes(input);
        Assert.assertEquals(false, uut.noteChanged());
    }
*/
    public void setNISettings(int measurementWindowMs, int minNoteLenMs,
                              int octaveSpan, int deviationWhereBorderlineStarts) {
        NoteAndDeviationIdentifier.NoteIdentifierSettings s = new NoteAndDeviationIdentifier.NoteIdentifierSettings();
        s.measurementWindowMs = measurementWindowMs;
        s.minNoteLenMs = minNoteLenMs;
        s.octaveSpan = octaveSpan;
        s.deviationWhereBorderLineStarts = deviationWhereBorderlineStarts;
        uut = new NoteAndDeviationIdentifier(s);
    }
}
