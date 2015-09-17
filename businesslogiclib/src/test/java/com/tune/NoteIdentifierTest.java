package com.tune;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by mart22n on 15.09.2015.
 */
public class NoteIdentifierTest {
    private NoteIdentifier noteIdentifier;

    @Before
    public void setUp() {
        setNISettings(30);
    }

    @Test
    public void inCaseOfMultipleZeroFreqWindowsInARow_pauseIsReturned() {
        double[] input = new double[] { 0, 0, 0 };
        Note[] notes = noteIdentifier.convertWaveformToNotes(input);
        Assert.assertEquals(1, notes.length);
        Assert.assertEquals(0, notes[0].type());
        Assert.assertEquals(90, notes[0].duration());
    }

    @Test
    public void whenTwoNoteFreqsWithTransitionAreInputted_twoNotesAreReturned() {

    }

    @Test
    public void whenTwoIdenticalFreqsWithLittleEnoughNoiseInBetweenAreInputted_singleNoteIsReturned() {

    }

    @Test
    public void whenTwoIdenticalFreqsWithLittleEnoughPauseInBetweenAreInputted_singleNoteIsReturned() {

    }

    @Test
    public void whenTwoIdenticalFreqsWithLongEnoughNoiseInBetweenAreInputted_threeNotesAreReturned() {

    }

    @Test
    public void whenTwoDifferentFreqsWithNoiseInBetweenAreInputted_twoNotesAreReturned() {

    }

    @Test
    public void whenTwoDifferentFreqsInARowAreInputted_twoNotesAreReturned() {

    }

    @Test
    public void whenTwoDifferentFreqsWhichAreInSameZoneAreInputted_singleNoteIsReturned() {

    }

    @Test
    public void whenBorderlineFreqIsInputted_noteWithTypeBorderlineIsReturned() {

    }

    @Test
    public void whenOneOctaveHigherFreqThanReferenceFreqIsInputted_correctNoteIsReturned() {

    }

    public void setNISettings(double measurementWindowMs) {
        NoteIdentifier.NoteIdentifierSettings s = new NoteIdentifier.NoteIdentifierSettings();
        s.measurementWindowMs = measurementWindowMs;
        noteIdentifier = new NoteIdentifier(s);
    }
}
