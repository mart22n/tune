package com.tune.businesslogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mart22n on 25.08.2015.
 * Zone means a frequency area around a note's frequency, where frequencies are considered as the
 * same note. The zone outside of that freq is borderline zone, where note cannot be identified
 */
public class NoteAndDeviationIdentifier {

    public static class NoteIdentifierSettings {
        public int measurementWindowMs;
        public int minNoteLenMs;
        public int octaveSpan; // this many octaves can a note be below or above reference freq to still be valid
        public int deviationWhereBorderLineStarts;
    }

    private int measurementWindowMs;
    private int minNoteLenMs;   // Any distinct note shorter than this length is considered a glitch and concatenated to previous valid note,
                                // or prepended to valid note if glitch is first window in recording, or if glitch is first window after valid note
    private double referenceFreq;
    private double[] freqsOfDegrees;
    private int octaveSpan;
    private int deviationWhereBorderLineStarts;
    private int curNonNoiseNoteStartIndexInInputWindows;
    private int curNoiseNoteStartIndexInInputWindows;
    private Note curNote = new Note();
    private Note prevNote = new Note();
    private boolean noteChanged = false;

    NoteAndDeviationIdentifier(NoteIdentifierSettings settings) {
        this.measurementWindowMs = settings.measurementWindowMs;
        this.minNoteLenMs = settings.minNoteLenMs;
        this.referenceFreq = -1;
        this.octaveSpan = settings.octaveSpan;
        this.deviationWhereBorderLineStarts = settings.deviationWhereBorderLineStarts;

    }

    boolean noteChanged() {
        boolean ret = false;
        if(!prevNote.hasSameTypeOrDegree(curNote)) {
            ret = true;
        }
        prevNote = curNote;
        return ret;
    }

    Note[] convertWaveformToNotes(double[] inputWindows) {
        List<Note> notes = new ArrayList<>();
        Note curNoiseNote = new Note();
        curNoiseNote.type = Note.NoteType.NOISE;
        Note curNonNoiseNote = new Note();
        Note.NoteType curNoteType;

        getReferenceFreqAndFindDegreeFreqs(inputWindows);
        if(referenceFreq == -1) {
            return notes.toArray(new Note[notes.size()]);
        }

        curNonNoiseNoteStartIndexInInputWindows = 0;
        curNoiseNoteStartIndexInInputWindows = 0;

        for (int i = 0; i < inputWindows.length; ++i) {
            curNoteType = getNoteTypeFromInput(inputWindows[i]);
            Note curWindowNote = initializeNote(inputWindows[i]);
            if (i > 0) {
                /*if(newNoteBegan(inputWindows, curNoteType, i)) {
                    /*[o] if curNon long enough: 1) Do lookahead: if window ends after curnon with a glitch, merge glitch into curnon and add curNon to notes. break (case a)
                    2) [ ] if window lasts a long time after curnon, do a lookahead to find if we have a glitch after curnon, followed by a long
                    continuation of curnon. Merge the glitch into curnon. If the lookahead returns true, concatenate the following continuation
                    into curnon. If after the continuation note there are no more windows,
                     add curNon to notes and break (case c). If after the continuation note there are more windows, update curNoiseIndex and
                     curNonIndex to point right after the continuation note. (case d) 2.5) [ ] If instead of the continuation note
                     there is a different long note, add curNon to notes and update both curNonStartIndex and curNoiseStartIndex to point right after the
                      glitch (case e)
                     3) [ ] if window lasts a long time after curnon, do a lookahead to find if we have a glitch after curnon, followed by a long
                    continuation of curnon. If the lookahead returns false and we have a long enough noise after curnon, add curNon to notes, update
                    curNonIndex. After that start doing a lookahead search to find out where the long enough noise ends: the noise ends, where we have 4) [ ]
                    a big enough number of similar note windows after the noise. Add curNoise to notes when we have identified the length of noise,
                    and update curNoiseIndex and curNonIndex both to point right after the noise. (case i) 5) similar case as 4, but we don't have any
                    more windows after the noise. Then add noise to notes and break (case j). 5.5) [ ] Do lookahead: if window continues after next long enough note,
                    update curNonIndex and curNoiseIndex to point at the start of next long enough note; assign curWindowNote to curNon and curNoise; continue the loop
                    (case k, l)
                     6) [ ] if curnon not long enough && curnoise not long enough, do lookahead, to find out where long enough nonnoise note starts.
                     When found, update curNonIndex to that position. Find the end of curNon, and merge
                     curNoise into curnon; then add curNon to notes. If no more windows, break (case b)
                     7) [ ] Same as 6, but instead of "if no more windows, break", we have more windows. Update curNonIndex and curNoiseIndex
                      to point right after end of curNOn; assign curWindowNote to curNon and curNoise and do a continue-clause (case g and f)
                      8) [ ] if curnon not long enough && curnoise long enough, do lookahead, to find out where long enough nonnoise starts. Add
                      curNoise to notes, update curNoiseIndex and curNonIndex to point at the beginning of nonnoise; assign curWindowNote to curNon
                      and curNoise and continue the loop (case h). If new note did not begin, just concatenate curWindowNote to curNon and to curNoise.
                     */
                if (newNoteBegan(inputWindows, curNoteType, i)) {
                    if (noteLongEnough(curNonNoiseNote)) {
                        if (inputWindows.length < i + minNoteLenMs / measurementWindowMs) {
                            // case a
                            mergeGlitchIntoAdjacentOrSurroundingNoteIfPossible(curNonNoiseNote, curNoiseNote, (inputWindows.length - i) * measurementWindowMs);
                            notes.add(curNonNoiseNote);
                            curNote = new Note(curNonNoiseNote);
                            break;
                        } else {
                            // case c
                            int nextNonNoiseNoteStartIndex = i;
                            boolean sameNoteContinuesAfterNoise = false;
                            boolean sameNoteContinuesForALongTimeUntilEndOfInput = false;
                            boolean sameNoteContinuesForAShortTimeUntilEndOfInput = false;
                            boolean differentNoteStartsAfterNoise = false;
                            for (int j = i; j < inputWindows.length; ++j) {
                                if (curNonNoiseNote.hasSameTypeOrDegree(initializeNote(inputWindows[j]))) {
                                    sameNoteContinuesAfterNoise = noteContinuesAfterNoise(inputWindows, curNonNoiseNote, j);
                                    if (sameNoteContinuesAfterNoise) {
                                        nextNonNoiseNoteStartIndex = j;
                                    }
                                    sameNoteContinuesForALongTimeUntilEndOfInput = sameNoteContinuesForALongTimeUntilEndOfInput(inputWindows, curNonNoiseNote, j);
                                    if(!sameNoteContinuesForALongTimeUntilEndOfInput) {
                                        sameNoteContinuesForAShortTimeUntilEndOfInput = sameNoteContinuesForAShortTimeUntilEndOfInput(inputWindows, curNonNoiseNote, j);
                                    }
                                    break;
                                }
                            }

                            if (sameNoteContinuesAfterNoise) {
                                if (sameNoteContinuesForALongTimeAfterGlitch(i, nextNonNoiseNoteStartIndex)) {
                                    if (sameNoteContinuesForALongTimeUntilEndOfInput) {
                                        mergeGlitchIntoAdjacentOrSurroundingNoteIfPossible(curNonNoiseNote, curNoiseNote, (nextNonNoiseNoteStartIndex - i) * measurementWindowMs);
                                        Note notesOtherPartAfterGlitch = initializeNote(inputWindows[nextNonNoiseNoteStartIndex]);
                                        for (int j = nextNonNoiseNoteStartIndex + 1; j < inputWindows.length; ++j) {
                                            notesOtherPartAfterGlitch.concatenate(initializeNote((inputWindows[j])));
                                        }
                                        curNonNoiseNote.concatenate(notesOtherPartAfterGlitch);
                                        notes.add(curNonNoiseNote);
                                        curNote = new Note(curNonNoiseNote);

                                        break;
                                    } else {
                                        // case d
                                        int lenOfNotesOtherPartAfterGlitch = 1;
                                        Note tmp = initializeNote(inputWindows[nextNonNoiseNoteStartIndex]);
                                        for (int j = nextNonNoiseNoteStartIndex + 1; j < inputWindows.length; ++j) {
                                            if (initializeNote(inputWindows[j]).hasSameTypeOrDegree(tmp)) {
                                                ++lenOfNotesOtherPartAfterGlitch;
                                            } else break;
                                        }

                                        Note notesOtherPartAfterGlitch = initializeNote(inputWindows[nextNonNoiseNoteStartIndex]);
                                        for (int j = 1; j < lenOfNotesOtherPartAfterGlitch; ++j) {
                                            notesOtherPartAfterGlitch.concatenate(initializeNote((inputWindows[j])));
                                        }

                                        mergeGlitchIntoAdjacentOrSurroundingNoteIfPossible(curNonNoiseNote, curNoiseNote, (nextNonNoiseNoteStartIndex - i) * measurementWindowMs);
                                        curNonNoiseNote.concatenate(notesOtherPartAfterGlitch);

                                        notes.add(curNonNoiseNote);
                                        curNote = new Note(curNonNoiseNote);

                                        curNonNoiseNoteStartIndexInInputWindows = nextNonNoiseNoteStartIndex + lenOfNotesOtherPartAfterGlitch;
                                        curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                        i = curNonNoiseNoteStartIndexInInputWindows - 1;
                                        curNonNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                    }
                                } else {
                                    // case i
                                    if(sameNoteContinuesForAShortTimeAfterGlitch(curNonNoiseNote, inputWindows, nextNonNoiseNoteStartIndex)) {
                                        curNonNoiseNote.makeLonger((nextNonNoiseNoteStartIndex - i) * measurementWindowMs, nextNonNoiseNoteStartIndex - i);
                                    }
                                    notes.add(curNonNoiseNote);
                                    curNote = new Note(curNonNoiseNote);

                                    curNoiseNote = initializeNote(inputWindows[i]);
                                    curNoiseNote.type = Note.NoteType.NOISE;
                                    for (int j = i + 1; j < nextNonNoiseNoteStartIndex; ++j) {
                                        curNoiseNote.concatenate(initializeNote(inputWindows[j]));
                                    }

                                    notes.add(curNoiseNote);
                                    curNote = new Note(curNoiseNote);
                                    curNonNoiseNoteStartIndexInInputWindows = nextNonNoiseNoteStartIndex;
                                    curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                    i = curNonNoiseNoteStartIndexInInputWindows - 1;
                                    curNonNoiseNote = initializeNote(inputWindows[i]);
                                    curNoiseNote = initializeNote(inputWindows[i]);
                                    curNoiseNote.type = Note.NoteType.NOISE;
                                }
                            } else {
                                differentNoteStartsAfterNoise = differentNoteStartsAfterNoise(inputWindows, i);
                                if(!differentNoteStartsAfterNoise) {
                                    if (sameNoteContinuesForALongTimeUntilEndOfInput) {
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                        for (int j = i + 1; j < nextNonNoiseNoteStartIndex; ++j) {
                                            curNoiseNote.concatenate(initializeNote(inputWindows[j]));
                                        }
                                        mergeGlitchIntoAdjacentOrSurroundingNoteIfPossible(curNonNoiseNote, curNoiseNote, (inputWindows.length - i) * measurementWindowMs);
                                    } else {

                                        if (sameNoteContinuesForAShortTimeUntilEndOfInput) {
                                            mergeGlitchIntoAdjacentOrSurroundingNoteIfPossible(curNonNoiseNote, curNoiseNote, (inputWindows.length - i) * measurementWindowMs);
                                            notes.add(curNonNoiseNote);
                                            curNote = new Note(curNonNoiseNote);
                                            break;
                                        }
                                        if (nextNonNoiseNoteStartIndex == i) {
                                            // case k
                                            notes.add(curNonNoiseNote);
                                            curNote = new Note(curNonNoiseNote);
                                            curNonNoiseNoteStartIndexInInputWindows = i;
                                            curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                            curNonNoiseNote = initializeNote(inputWindows[i]);
                                            curNoiseNote = initializeNote(inputWindows[i]);
                                            curNoiseNote.type = Note.NoteType.NOISE;
                                            continue;
                                        }
                                    }
                                }
                                else {

                                    if (differentNoteStartsAfterNoise) {
                                        nextNonNoiseNoteStartIndex = findStartIndexOfNextLongEnoughNonNoiseNote(inputWindows, i);
                                        if(nextNonNoiseNoteStartIndex == i) {
                                            // no noise between notes
                                            curNonNoiseNoteStartIndexInInputWindows = nextNonNoiseNoteStartIndex;
                                            curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                            notes.add(curNonNoiseNote);
                                            curNote = new Note(curNonNoiseNote);
                                            curNonNoiseNote = initializeNote(inputWindows[i]);
                                            curNoiseNote = initializeNote(inputWindows[i]);
                                            curNoiseNote.type = Note.NoteType.NOISE;
                                            continue;
                                        }
                                        // case e
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                        for (int j = i + 1; j < nextNonNoiseNoteStartIndex; ++j) {
                                            curNoiseNote.concatenate(initializeNote(inputWindows[j]));
                                        }
                                        curNonNoiseNote.makeLonger((nextNonNoiseNoteStartIndex - i) * measurementWindowMs, (nextNonNoiseNoteStartIndex - i));
                                        notes.add(curNonNoiseNote);
                                        curNote = new Note(curNonNoiseNote);
                                        curNonNoiseNoteStartIndexInInputWindows = nextNonNoiseNoteStartIndex;
                                        curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                        i = curNonNoiseNoteStartIndexInInputWindows - 1;
                                        curNonNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                    } else {
                                        // case j
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                        for (int j = i + 1; j < inputWindows.length; ++j) {
                                            curNoiseNote.concatenate(initializeNote(inputWindows[j]));
                                        }
                                        notes.add(curNoiseNote);
                                        curNote = new Note(curNoiseNote);
                                        break;
                                    }
                                }
                            }
                        }

                    } else {
                        if (!noteLongEnough(curNoiseNote)) {
                            int noiseStartIndex = findStartIndexOfNextLongEnoughNoiseNote(inputWindows, i);
                            int nonNoiseNoteStartIndex = findStartIndexOfNextLongEnoughNonNoiseNote(inputWindows, i);

                            if(noiseStartIndex > i && noiseStartIndex < nonNoiseNoteStartIndex && nonNoiseNoteStartIndex -
                                    noiseStartIndex >= minNoteLenMs / measurementWindowMs) {
                                curNoiseNote.type = Note.NoteType.NOISE;
                                for (int j = curNoiseNoteStartIndexInInputWindows + 1; j < nonNoiseNoteStartIndex; ++j) {
                                    curNoiseNote.concatenate(initializeNote(inputWindows[j]));
                                }
                                notes.add(curNoiseNote);
                                curNote = new Note(curNoiseNote);
                                curNonNoiseNoteStartIndexInInputWindows = nonNoiseNoteStartIndex;
                                curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                i = curNonNoiseNoteStartIndexInInputWindows - 1;
                                curNoiseNote = initializeNote(inputWindows[i]);
                                curNoiseNote.type = Note.NoteType.NOISE;
                                curNonNoiseNote = initializeNote(inputWindows[i]);
                            }
                            else {
                                if (nonNoiseNoteStartIndex >= i) {
                                    int curNonNoiseNoteLenBeforeMergingGlitch = findNonNoiseNoteLen(inputWindows, nonNoiseNoteStartIndex);
                                    curNonNoiseNote = initializeNote(inputWindows[nonNoiseNoteStartIndex]);
                                    for (int j = 1; j < curNonNoiseNoteLenBeforeMergingGlitch; ++j) {
                                        curNonNoiseNote.concatenate(initializeNote(inputWindows[nonNoiseNoteStartIndex + j]));
                                    }

                                    if (nonNoiseNoteStartIndex - i < minNoteLenMs / measurementWindowMs) {

                                        curNonNoiseNote.makeLonger((nonNoiseNoteStartIndex - curNoiseNoteStartIndexInInputWindows) * measurementWindowMs,
                                                nonNoiseNoteStartIndex - curNoiseNoteStartIndexInInputWindows, false);
                                        if (notes.size() > 0 && notes.get(notes.size() - 1).hasSameTypeOrDegree(curNonNoiseNote)) {
                                            notes.get(notes.size() - 1).concatenate(curNonNoiseNote);
                                        } else {
                                            notes.add(curNonNoiseNote);
                                            curNote = new Note(curNonNoiseNote);
                                        }
                                        if (nonNoiseNoteStartIndex + curNonNoiseNoteLenBeforeMergingGlitch == inputWindows.length)
                                            // case b
                                            break;
                                        else {
                                            // case g and f
                                            curNonNoiseNoteStartIndexInInputWindows = nonNoiseNoteStartIndex + curNonNoiseNoteLenBeforeMergingGlitch;
                                            curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                            i = curNonNoiseNoteStartIndexInInputWindows - 1;
                                            curNoiseNote = initializeNote(inputWindows[i]);
                                            curNoiseNote.type = Note.NoteType.NOISE;
                                            curNonNoiseNote = initializeNote(inputWindows[i]);
                                        }
                                    } else {
                                        // long enough noise before long nonnoise note
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                        for (int j = i + 1; j < nonNoiseNoteStartIndex; ++j) {
                                            curNoiseNote.concatenate(initializeNote(inputWindows[j]));
                                        }
                                        notes.add(curNoiseNote);
                                        curNote = new Note(curNoiseNote);
                                        curNonNoiseNoteStartIndexInInputWindows = nonNoiseNoteStartIndex;
                                        curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                        i = curNonNoiseNoteStartIndexInInputWindows - 1;
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                        curNonNoiseNote = initializeNote(inputWindows[i]);
                                    }
                                } else {
                                    int startIndexOfNextLongEnoughNoise = findStartIndexOfNextLongEnoughNoiseNote(inputWindows, i);
                                    if (startIndexOfNextLongEnoughNoise >= i) {
                                        curNoiseNote = initializeNote(inputWindows[i]);
                                        curNoiseNote.type = Note.NoteType.NOISE;
                                        for (int j = i + 1; j < inputWindows.length; ++j) {
                                            curNoiseNote.concatenate(initializeNote(inputWindows[j]));
                                        }
                                        notes.add(curNoiseNote);
                                        curNote = new Note(curNoiseNote);
                                        break;
                                    } else {
                                        if (notes.size() > 0) {
                                            notes.get(notes.size() - 1).makeLonger((inputWindows.length - i) * measurementWindowMs, inputWindows.length - i);
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            int nonNoiseNoteStartIndex = findStartIndexOfNextLongEnoughNonNoiseNote(inputWindows, i);
                            if (nonNoiseNoteStartIndex >= i) {
                                // case h
                                notes.add(curNoiseNote);
                                curNote = new Note(curNoiseNote);
                                curNonNoiseNoteStartIndexInInputWindows = nonNoiseNoteStartIndex;
                                curNoiseNoteStartIndexInInputWindows = curNonNoiseNoteStartIndexInInputWindows;
                                i = curNonNoiseNoteStartIndexInInputWindows - 1;
                                curNoiseNote = initializeNote(inputWindows[i]);
                                curNoiseNote.type = Note.NoteType.NOISE;
                                curNonNoiseNote = initializeNote(inputWindows[i]);
                            }
                            else {
                                if (inputWindows.length < i + minNoteLenMs / measurementWindowMs) {
                                    mergeGlitchIntoAdjacentOrSurroundingNoteIfPossible(curNonNoiseNote, curNoiseNote, (inputWindows.length - i) * measurementWindowMs);
                                    notes.add(curNonNoiseNote);
                                    curNote = new Note(curNonNoiseNote);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    curNoiseNote.concatenate(curWindowNote);
                    curNonNoiseNote.concatenate(curWindowNote);
                    if(i == inputWindows.length - 1 && noteLongEnough(curNonNoiseNote)) {
                        notes.add(curNonNoiseNote);
                        curNote = new Note(curNonNoiseNote);
                    }
                }
            }
            else {
                curNoiseNote.concatenate(curWindowNote);
                curNonNoiseNote.concatenate(curWindowNote);
            }
        }
        return notes.toArray(new Note[notes.size()]);
    }

    private int findNonNoiseNoteLen(double[] inputWindows, int nonNoiseNoteStartIndex) {
        Note tmp = initializeNote(inputWindows[nonNoiseNoteStartIndex]);
        int ret = 1;
        for (int j = nonNoiseNoteStartIndex + 1; j < inputWindows.length; ++j) {
            if(tmp.hasSameTypeOrDegree(initializeNote(inputWindows[j]))) {
                ++ret;
            }
            else {
                break;
            }
        }
        return ret;
    }

    private int findStartIndexOfNextLongEnoughNonNoiseNote(double[] inputWindows, int searchStartIndex) {
        int ret = -1;
        for (int j = searchStartIndex; j < inputWindows.length && ret == -1; ++j) {
            int nonNoiseNoteLen = 1;
            for (int k = j + 1; k < inputWindows.length; ++k) {
                if(freqsOfDegrees == null) {
                    if(inputWindows[k] == inputWindows[j]) {
                        ++nonNoiseNoteLen;
                    }
                    else {
                        break;
                    }
                }
                else {
                    if (initializeNote(inputWindows[k]).hasSameTypeOrDegree(initializeNote(inputWindows[j]))) {
                        ++nonNoiseNoteLen;
                    } else {
                        break;
                    }
                }
                if(nonNoiseNoteLen >= minNoteLenMs / measurementWindowMs) {
                    ret = j;
                    break;
                }
            }
        }
        return ret;
    }

    private int findStartIndexOfNextLongEnoughNoiseNote(double[] inputWindows, int searchStartIndex) {
        int ret = -1;
        int noiseNoteLen = 1;
        int nonNoiseNoteLen = 1;
        for (int j = searchStartIndex + 1; j < inputWindows.length && ret == -1; ++j) {
            if(!initializeNote(inputWindows[j]).hasSameTypeOrDegree(initializeNote(inputWindows[j - 1]))) {
                ++noiseNoteLen;
            }
            else {
                ++nonNoiseNoteLen;
            }
            if(noiseNoteLen >= minNoteLenMs / measurementWindowMs) {
                ret = j + 1 - minNoteLenMs / measurementWindowMs;
                break;
            }
            if(nonNoiseNoteLen >= minNoteLenMs / measurementWindowMs) {
                noiseNoteLen = 0;
            }
        }
        return ret;
    }

    private boolean differentNoteStartsAfterNoise(double[] inputWindows, int i) {
        boolean ret = false;
        for (int j = i; j < inputWindows.length && !ret; ++j) {
            int noteLen = 1;
            for (int k = j + 1; k < inputWindows.length; ++k) {
                if(initializeNote(inputWindows[k]).hasSameTypeOrDegree(initializeNote(inputWindows[j]))) {
                    ++noteLen;
                }
                else {
                    break;
                }
                if(noteLen >= minNoteLenMs / measurementWindowMs) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    private boolean sameNoteContinuesForALongTimeUntilEndOfInput(double[] inputWindows, Note curNonNoiseNote, int j) {
        boolean ret = false;
        for (int k = j; k < inputWindows.length; ++k) {
            if(!curNonNoiseNote.hasSameTypeOrDegree(initializeNote(inputWindows[k])))
                break;
            if(k == inputWindows.length - 1 && k >= j + minNoteLenMs / measurementWindowMs - 1) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean sameNoteContinuesForAShortTimeUntilEndOfInput(double[] inputWindows, Note curNonNoiseNote, int j) {
        boolean ret = false;
        for (int k = j; k < inputWindows.length; ++k) {
            if(!curNonNoiseNote.hasSameTypeOrDegree(initializeNote(inputWindows[k])))
                break;
            if(k == inputWindows.length - 1 && k < j + minNoteLenMs / measurementWindowMs - 1) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean noteContinuesAfterNoise(double[] inputWindows, Note curNonNoiseNote, int j) {
        boolean ret = false;
        for (int k = j; k < j + minNoteLenMs / measurementWindowMs && k < inputWindows.length; ++k) {
            if(!curNonNoiseNote.hasSameTypeOrDegree(initializeNote(inputWindows[k])))
                break;
            if(k == j + minNoteLenMs / measurementWindowMs - 1) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean sameNoteContinuesForALongTimeAfterGlitch(int i, int continuationNoteStartIndex) {
        return continuationNoteStartIndex - i < minNoteLenMs / measurementWindowMs;
    }

    private boolean sameNoteContinuesForAShortTimeAfterGlitch(Note curNote, double[] inputWindows, int continuationNoteStartIndex) {
        boolean ret = false;
        int differentNoteStartIndex = -1;
        for (int k = continuationNoteStartIndex; k < continuationNoteStartIndex + minNoteLenMs / measurementWindowMs; ++k) {
            if (!curNote.hasSameTypeOrDegree(initializeNote(inputWindows[k]))) {
                differentNoteStartIndex = k;
                break;
            }
        }
        if(differentNoteStartIndex >= 0 && differentNoteStartIndex < continuationNoteStartIndex + minNoteLenMs / measurementWindowMs)
            ret = true;
        return ret;
    }

    private void getReferenceFreqAndFindDegreeFreqs(double[] inputWindows) {
        if(referenceFreq != -1)
            return;
        int startIndexOfNote = 0;
        do {
            startIndexOfNote = findStartIndexOfNextLongEnoughNonNoiseNote(inputWindows, startIndexOfNote);
            if (startIndexOfNote >= 0) {
                if (inputWindows[startIndexOfNote] <= 0) {
                    ++startIndexOfNote;
                    continue;
                } else {
                    referenceFreq = inputWindows[startIndexOfNote];
                    calculateDegreeFreqs();
                    break;
                }
            }
        } while(startIndexOfNote >= 0);

        /*if(referenceFreq == -1) {
            for (int i = 0; i < inputWindows.length; ++i) {
                if(inputWindows[i] > 0) {
                    referenceFreq = inputWindows[i];
                    calculateDegreeFreqs();
                    break;
                }
            }
        }*/
    }

    private void mergeGlitchIntoAdjacentOrSurroundingNoteIfPossible(Note curNonNoiseNote, Note curNoiseNote, int howMuchToLengthenNoteToTheRightMs) {
        if(noteLongEnough(curNonNoiseNote)) {
            if (!noteLongEnough(curNoiseNote)) {
                if (curNoiseNoteStartIndexInInputWindows < curNonNoiseNoteStartIndexInInputWindows) {
                    curNonNoiseNote.makeLonger(curNoiseNote.lengthMs, curNoiseNote.lengthMs / measurementWindowMs, false);
                    curNonNoiseNoteStartIndexInInputWindows = curNoiseNoteStartIndexInInputWindows;
                } else if (curNoiseNoteStartIndexInInputWindows > curNonNoiseNoteStartIndexInInputWindows) {
                    curNonNoiseNote.makeLonger(curNoiseNote.lengthMs, curNoiseNote.lengthMs / measurementWindowMs);
                    curNoiseNoteStartIndexInInputWindows += curNoiseNote.lengthMs / measurementWindowMs;
                }
            } else {
                curNonNoiseNote.makeLonger(howMuchToLengthenNoteToTheRightMs, howMuchToLengthenNoteToTheRightMs / measurementWindowMs);
            }
        }
    }

    private Note initializeNote(double freq) {
        Note ret;
        ret = new Note();
        ret.type = getNoteTypeFromInput(freq);
        if(ret.type == Note.NoteType.VALIDNOTE || ret.type == Note.NoteType.BORDERLINE) {
            ret.degree = nearestDegreeToTheFreq(freq);
            ret.addDeviation(calcDeviation(freq,
                    ret.degree), freqIsBorderline(freq, ret.degree));
        }
        ret.lengthMs = measurementWindowMs;
        return ret;
    }

    private boolean noteLongEnough(Note note) {
        return note.lengthMs >= minNoteLenMs;
    }


    private boolean newNoteBegan(double[] inputWindows, Note.NoteType curNoteType, int i) {
        return getNoteTypeFromInput(inputWindows[i]) != curNoteType ||
                freqsAreDifferentNotes(inputWindows[i - 1], inputWindows[i]);
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
        if(freqsOfDegrees == null)
            return 0;
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
