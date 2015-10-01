package com.tune;

import com.tune.businesslogic.Note;

/**
 * Created by mart22n on 25.08.2015.
 */
public class ChartController {
    void drawNotes(Note[] notes) {
        /*
        Algo: 1)
         */
        //throw new UnsupportedOperationException();
       /* ArrayList<Entry> note1 = new ArrayList<Entry>();
        Entry e = new Entry(40f, 0);
        note1.add(e);
        e = new Entry(40f, 1);
        note1.add(e);

        ArrayList<Entry> note2 = new ArrayList<Entry>();
        e = new Entry(40f, 3);
        note2.add(e);
        e = new Entry(35f, 4);
        note2.add(e);
        e = new Entry(45f, 5);
        note2.add(e);

        ArrayList<Entry> note3 = new ArrayList<Entry>();
        e = new Entry(0f, 6);
        note3.add(e);
        e = new Entry(10f, 7);
        note3.add(e);
        e = new Entry(-20f, 8);
        note3.add(e);
        e = new Entry(-20f, 9);
        note3.add(e);

        LineDataSet dsNote1 = new LineDataSet(note1, "");
        dsNote1.setDrawCubic(true);
        dsNote1.setCubicIntensity(0.2f);
        dsNote1.setDrawFilled(true);
        dsNote1.setFillColor(Color.WHITE);
        dsNote1.setFillAlpha(254);

        LineDataSet dsNote2 = new LineDataSet(note2, "");
        dsNote2.setDrawCubic(true);
        dsNote2.setCubicIntensity(0.2f);
        dsNote2.setDrawFilled(true);
        dsNote2.setFillColor(Color.WHITE);
        dsNote2.setFillAlpha(254);


        LineDataSet dsNote3 = new LineDataSet(note3, "");
        dsNote3.setDrawCubic(true);
        dsNote3.setCubicIntensity(0.2f);
        dsNote3.setDrawFilled(true);
        dsNote3.setFillColor(Color.WHITE);
        dsNote3.setFillAlpha(255);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(dsNote1);
        dataSets.add(dsNote2);
        dataSets.add(dsNote3);

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("E"); xVals.add(""); xVals.add(""); xVals.add("F");
        xVals.add(""); xVals.add(""); xVals.add("G"); xVals.add("");
        xVals.add("");

        LineData data = new LineData(xVals, dataSets);
        lineChart.setData(data);
        lineChart.invalidate(); // refresh*/
    }
}
