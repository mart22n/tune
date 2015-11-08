package com.tune;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.tune.businesslogic.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mart22n on 25.08.2015.
 */
public class ChartController {

    private int rollingSpeedCmPerSecond = 1;
    private int minNoteLenMs = 100;
    private int mWidthPixels;
    private LineChart lineChart;
    private MainActivity mainActivity;
    private int chartWidthCm;
    private int orientation;
    private ArrayList<ArrayList<Double>> addableNotes = new ArrayList<>();
    private int noteIndex = 0;
    private ArrayList<String> noteNames = new ArrayList<>();
    private int noteNameIndex = 0;
    private int curXIndex = 0;
    private int sleepTimer = 0;
    private int maxViewPortSize = 10;

    public ChartController(int rollingSpeedCmPerSecond, int minNoteLenMs, LineChart lineChart, MainActivity mainActivity) {
        this.rollingSpeedCmPerSecond = rollingSpeedCmPerSecond;
        this.minNoteLenMs = minNoteLenMs;
        this.lineChart = lineChart;
        this.mainActivity = mainActivity;
        noteNames.add("1");
        noteNames.add("3");
        noteNames.add("9");
        noteNames.add("6");
        noteNames.add("11");
        noteNames.add("-3");
        noteNames.add("4");
        noteNames.add("");
        noteNames.add("5");
        noteNames.add("5");
        createNotes();
       /* for(int i = 0; i < 1000; i += 1) {
            ArrayList<Double> note = new ArrayList<>();
            int randCnt = (int)(Math.random() * 10);
            int sign = (Math.random() < 0.5 ? -1 : 1);
            for(int j = 0; j < 6; ++j) {
                note.add(sign * Math.random() / 2.2);
            }
            addableNotes.add(note);
        }*/
    }

    private void createNotes() {
        ArrayList<Double> note = new ArrayList<>();
        note.add(0.1);
        note.add(0.15);
        note.add(0.05);
        note.add(0.1);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.1);
        note.add(0.15);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.1);
        note.add(0.15);
        note.add(0.1);
        note.add(0.25);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.1);
        note.add(0.15);
        note.add(0.21);
        note.add(0.10);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.3);
        note.add(0.25);
        note.add(0.05);
        note.add(0.07);
        note.add(0.15);
        note.add(0.25);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.1);
        note.add(0.15);
        note.add(0.05);
        note.add(0.1);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.1);
        note.add(0.25);
        note.add(0.15);
        note.add(0.1);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.0);
        note.add(0.0);
        note.add(0.0);
        note.add(0.0);
        addableNotes.add(note);

        note = new ArrayList<>();
        note.add(0.3);
        note.add(0.45);
        note.add(0.45);
        note.add(0.35);
        note.add(0.30);
        note.add(0.28);
        addableNotes.add(note);


        note = new ArrayList<>();
        note.add(-0.2);
        note.add(-0.3);
        note.add(-0.1);
        note.add(-0.2);
        addableNotes.add(note);
    }

    private void setRealDeviceSizeInPixels() {
        Display display = mainActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);


        // since SDK_INT = 1;
        mWidthPixels = displayMetrics.widthPixels;

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
            } catch (Exception ignored) {
            }
        }

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                mWidthPixels = realSize.x;
            } catch (Exception ignored) {
            }
        }
    }

    public void initChart(int orientation) {
        setRealDeviceSizeInPixels();
        DisplayMetrics dm = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        chartWidthCm = (int)(mWidthPixels/dm.xdpi * 2.54);

        lineChart.setGridBackgroundColor(Color.BLACK);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(false);
        lineChart.setDragDecelerationFrictionCoef(0.85f);
        this.orientation = orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT)
            lineChart.setMinimumHeight(475);
        else
            lineChart.setMinimumHeight(445);
        lineChart.setDescription("");
        lineChart.getLegend().setEnabled(false);

        lineChart.getXAxis().setEnabled(true);
        lineChart.getXAxis().setDrawAxisLine(true);
        lineChart.getXAxis().setAxisLineColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        // define where to add labels of x-axis
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setGridColor(Color.WHITE);
        lineChart.getXAxis().setTextSize(22);
/*        LimitLine ll = new LimitLine(2f, "150");
    ll.setLineColor(Color.YELLOW);
    ll.setLineWidth(4f);
    lineChart.getXAxis().addLimitLine(ll);*/

        lineChart.getAxisLeft().setAxisMaxValue(0.55f);    // max deviation = +/-50 cents
        lineChart.getAxisLeft().setAxisMinValue(-0.55f);
        lineChart.getAxisLeft().setStartAtZero(false);

        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisLeft().setDrawAxisLine(true);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setAxisLineColor(Color.WHITE);
        lineChart.getAxisLeft().setGridColor(Color.WHITE);




        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        LineDataSet dsNote1 = new LineDataSet(new ArrayList<Entry>(), "");

        for(int i = 0; i < maxViewPortSize; ++i) {
            Entry e = new Entry(0, i);
            dsNote1.addEntry(e);
            xVals.add("");
        }

        formatDataSet(dsNote1);
        dataSets.add(dsNote1);

        LineData data = new LineData(xVals, dataSets);
        lineChart.setData(data);
        lineChart.invalidate(); // refresh
    }

    private void formatDataSet(LineDataSet lds) {
        lds.setDrawCubic(true);
        lds.setCubicIntensity(0.2f);
        lds.setDrawFilled(true);
        lds.setFillColor(Color.WHITE);
        lds.setFillAlpha(255);
        lds.setDrawCircles(false);
        lds.setDrawValues(false);
        lds.setLineWidth(0.2f);
        lds.setValueTextSize(22f);
        lds.setValueTypeface(Typeface.create((String) null, Typeface.BOLD));
    }

    void drawNotes(Note[] notes) {
        LineData lineData = lineChart.getData();
        if(noteIndex == addableNotes.size())
            return;
        if (sleepTimer < addableNotes.get(noteIndex).size()) {
            ++sleepTimer;
            try {
                Thread.sleep(400);
            } catch (InterruptedException ex) {
            }
        } else {
            sleepTimer = 0;


            List<LineDataSet> existingNotes = lineData.getDataSets();

            ArrayList<Entry> displayedDeviations = new ArrayList<>();
            if (noteIndex < addableNotes.size()) {
                switch(noteIndex) {
                    case 0:
                    case 1:
                        Toast.makeText(mainActivity.getApplicationContext(), "Esimene valge kujund näitab, et noot on umbes 0,1 pooltooni kõrgem kui helistiku korrektne noot", Toast.LENGTH_LONG).show();
                    break;
                    case 2:
                    case 3:
                        Toast.makeText(mainActivity.getApplicationContext(), "Järgmised noodid on samuti veidi kõrgemad, kui peaks. Laineline osa näitab kõrvalekalde muutumist noodi jooksul, kujundi laius noodi pikkust", Toast.LENGTH_LONG).show();
                        break;
                    case 5:
                    case 6:
                        Toast.makeText(mainActivity.getApplicationContext(), "Noodi all olev number näitab, mitu pooltooni ta on kõrgem või madalam esimesena laudud noodist", Toast.LENGTH_LONG).show();
                        break;
                    case 7:
                        Toast.makeText(mainActivity.getApplicationContext(), "Pikemad vahed näitavad pause", Toast.LENGTH_LONG).show();
                        break;
                    case 8:
                    case 9:
                        Toast.makeText(mainActivity.getApplicationContext(), "Punane kujund on hoiatus: kõrvalekalle on suurem kasutaja seadistatud maksimumist. Antakse helisignaal", Toast.LENGTH_LONG).show();
                    break;
                }
                for (int j = 0; j < addableNotes.get(noteIndex).size(); ++j) {
                    Entry e = new Entry((addableNotes.get(noteIndex).get(j)).floatValue(), curXIndex + maxViewPortSize + j);
                    displayedDeviations.add(e);
                    if (j == (int)(addableNotes.get(noteIndex).size() / 2)) {
                        lineData.getXVals().add(noteNames.get(noteNameIndex++));
                    }
                    else {
                        lineData.getXVals().add("");
                    }
                }


                LineDataSet lds = new LineDataSet(displayedDeviations, "");
                formatDataSet(lds);
                if(noteIndex == 8) {
                    lds.setFillColor(Color.RED);
                }
                existingNotes.add(lds);
                curXIndex += addableNotes.get(noteIndex).size();
                ++noteIndex;
            }

            lineChart.setVisibleXRange(0, maxViewPortSize);

            lineChart.centerViewTo(lineData.getXValCount() - maxViewPortSize / 2, 0, YAxis.AxisDependency.RIGHT);

            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
/*
            if (noteIndex < 4 && orientation == Configuration.ORIENTATION_PORTRAIT)
        Toast.makeText(mainActivity.getApplicationContext(), "Each white column shows, how much in semitones, each note has deviated from the correct pitch.", Toast.LENGTH_LONG).show();
*/
    }
    }
}

   /*  int GRAPH_WIDTH = 10;
     LineData lineData = lineChart.getData();
     LineDataSet lineDataSet = lineData.getDataSetByIndex(0);
     int count = lineDataSet.getEntryCount();

     for(int j = 0; j < count; ++j) {

// Make rolling window
      if (lineData.getXValCount() <= count) {
       // Remove/Add XVal
       lineData.getXVals().add("" + count);
       lineData.getXVals().remove(0);

       // Move all entries 1 to the left..
       for (int i = 0; i < count; i++) {
        Entry e = lineDataSet.getEntryForXIndex(i);
        if (e == null) continue;

        e.setXIndex(e.getXIndex() - 1);
       }

       // Set correct index to add value
       count = GRAPH_WIDTH;
      }

// Add new value
      if(notes[j].type == Note.NoteType.VALIDNOTE) {
       lineData.addEntry(new Entry(notes[j].getDeviation(0), count), 0);
      }

// Make sure to draw
      lineChart.notifyDataSetChanged();
      lineChart.invalidate();
    }
}*/
