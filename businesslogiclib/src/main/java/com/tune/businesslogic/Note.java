package com.tune.businesslogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mart22n on 22.08.2015.
 */
public class Note {
    public enum NoteType { UNDEFINED, PAUSE, VALIDNOTE, NOISE, BORDERLINE, OUTOFRANGE };
    private String name;
    public int degree; // 0 - reference note; 12 - 1 octave up; -12 - 1 octave down
    public int lengthMs;
    public List<Integer> deviations; // list of deviations for the note
    public NoteType type;

    Note() {
        this.type = NoteType.UNDEFINED;
        this.deviations = new ArrayList<Integer>();
    }

    Note(Note orig) {
        this.type = orig.type;
        this.name = orig.name;
        this.degree = orig.degree;
        this.lengthMs = orig.lengthMs;
        this.deviations = orig.deviations;
    }

    void addDeviation(int deviation, boolean borderline) {
        deviations.add(deviation);
        if(borderline == true)
            this.type = NoteType.BORDERLINE;
    }

    public boolean hasSameTypeOrDegree(Note n2) {
        if(type == NoteType.VALIDNOTE || type == NoteType.BORDERLINE) {
            if (n2.type == type && n2.degree == degree) {
                return true;
            }
        }
        else if (type == n2.type) {
            return true;
        }
        return false;
    }

    public void makeLonger(int howManyMsToLengthen, int howManyDeviationsToAdd) {
        if(type == NoteType.VALIDNOTE || type == NoteType.BORDERLINE) {
            for (int i = 0; i < howManyDeviationsToAdd; ++i) {
                deviations.add(deviations.get(deviations.size() - 1));
            }
        }
        this.lengthMs += howManyMsToLengthen;
    }

    public void makeLonger(int howManyMsToLengthen, int howManyDeviationsToAdd, boolean addDeviationsToTheRight) {
        if(type == NoteType.VALIDNOTE || type == NoteType.BORDERLINE) {
            for (int i = 0; i < howManyDeviationsToAdd; ++i) {
                if(addDeviationsToTheRight == true) {
                    deviations.add(deviations.get(deviations.size() - 1));
                }
                else {
                    deviations.add(0, deviations.get(0));
                }
            }
        }
        this.lengthMs += howManyMsToLengthen;
    }

    public void concatenate(Note n2) {
        lengthMs += n2.lengthMs;
        degree = n2.degree;
        if(((type == NoteType.VALIDNOTE || type == NoteType.BORDERLINE) && (n2.type == Note.NoteType.VALIDNOTE || n2.type == Note.NoteType.BORDERLINE))) {
            for (int j = 0; j < n2.deviations.size(); ++j) {
                addDeviation(n2.getDeviation(j), n2.type == Note.NoteType.BORDERLINE);
            }
            if(n2.type == Note.NoteType.BORDERLINE) {
                type = Note.NoteType.BORDERLINE;
            }
        }
        if(type == NoteType.UNDEFINED)
            type = n2.type;
    }

    public int getDeviation(int index) {
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
