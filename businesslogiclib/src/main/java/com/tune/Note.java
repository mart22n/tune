package com.tune;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mart22n on 22.08.2015.
 */
class Note {
    enum NoteType { UNDEFINED, PAUSE, VALIDNOTE, NOISE, BORDERLINE, OUTOFRANGE };
    private String name;
    int degree; // 0 - reference note; 12 - 1 octave up; -12 - 1 octave down
    int length;
    private List<Integer> deviations; // list of deviations for the note
    NoteType type;

    Note() {
        this.type = NoteType.UNDEFINED;
        this.deviations = new ArrayList<Integer>();
    }

    Note(Note orig) {
        this.type = orig.type;
        this.name = orig.name;
        this.degree = orig.degree;
        this.length = orig.length;
        this.deviations = orig.deviations;
    }

    void addDeviation(int deviation, boolean borderline) {
        deviations.add(deviation);
        if(borderline == true)
            this.type = NoteType.BORDERLINE;
    }

    int getDeviation(int index) {
        return deviations.get(index);
    }

    private static String levelToStepName(int level) {
        switch(level) {
  /*          case 0:
                return "I";
            case 1:
                return "I#";
            case 2:
                return "II";
            case 3:
                return "II#";
            case 4: return "III";
            case 5: return "IV";
            case 6: return "IV#";
            case 7: return "V";
            case 8: return "V#";
            case 9: return "VI";
            case 10: return "VI#";
            case 11: return "VII";
            case 12: return "I'";*/
            case 0: return "0";
            case 1: return "1";
        }
        return "0";
    }

    private static String freqToNoteName(double freq) {
        switch((int)freq) {
            case 65: return "C2";
        }
        return "C2";
    }
}
