package com.tune.businesslogic;

/**
 * Created by mart22n on 25.08.2015.
 */
public interface BusinessLogicAdapterListener {

    public void onFirstNoteDetected(Note note);

    public void onNewNotesOrPausesAvailable(Note[] notes);

    public void onToastNotification(String notification);
}