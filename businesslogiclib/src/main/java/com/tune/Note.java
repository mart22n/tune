package com.tune;

import android.util.Pair;

import java.util.List;

/**
 * Created by mart22n on 22.08.2015.
 */
class Note {
    Note(NoteType t, String n, short dur, List<Pair<Short, Short>> dev) {
        type = t;
        name = n;
        duration = dur;
        deviations = dev;
    }
    enum NoteType { PAUSE, NOTE };
    private String name;
    private short duration;
    private List<Pair<Short, Short>> deviations; // list of deviations for the note
    private NoteType type;
}
