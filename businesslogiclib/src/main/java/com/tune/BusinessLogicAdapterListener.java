package com.tune;

import java.util.List;

/**
 * Created by mart22n on 25.08.2015.
 */
interface BusinessLogicAdapterListener {

    public void onFirstNoteDetected(Note note);

    public void onNewNotesOrPausesAvailable(List<Note> notes);
}
