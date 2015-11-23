package com.tune;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.tune.businesslogic.Note;
import com.tune.businesslogic.RollingAverageFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mart22n on 25.08.2015.
 */
public class ChartController {

    public static class ChartControllerSettings {
        public ChartControllerSettings(int rollingSpeedCmPerSec, int minNoteLenMs, int octaveSpan,
                                       int windowLenMs, int rollingAverageInputCount) {
            this.rollingSpeedCmPerSec = rollingSpeedCmPerSec;
            this.octaveSpan = octaveSpan;
            this.minNoteLenMs = minNoteLenMs;
            this.windowLenMs = windowLenMs;
            this.rollingAverageInputCount = rollingAverageInputCount;
        }
        int rollingSpeedCmPerSec, minNoteLenMs, octaveSpan, windowLenMs, rollingAverageInputCount;
    }

    private RollingAverageFinder rollingAverageFinder;
    private int rollingSpeedCmPerSecond = 1;
    private int minNoteLenMs = 100;
    private int mWidthPixels;
    private LineChart lineChart;
    private MainActivity mainActivity;
    private int chartWidthCm;
    private int orientation;
    private int curXIndex = 0;
    private int maxViewPortSize = 300;
    private int windowLenMs = 30;
    private int rollingAverageInputCount;

    public ChartController(ChartControllerSettings settings, LineChart lineChart, MainActivity mainActivity) {
        this.rollingSpeedCmPerSecond = settings.rollingSpeedCmPerSec;
        this.minNoteLenMs = settings.minNoteLenMs;
        this.lineChart = lineChart;
        this.mainActivity = mainActivity;
        this.windowLenMs = windowLenMs;
        rollingAverageInputCount = settings.rollingAverageInputCount;
        this.rollingAverageFinder = new RollingAverageFinder(settings.rollingAverageInputCount);
        maxViewPortSize /= rollingAverageInputCount;
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

        List<LineDataSet> existingNotes = lineData.getDataSets();
        for (int i = 0; i < notes.length; ++i) {
            if(notes[i].type == Note.NoteType.PAUSE) {
                for(int j = 0; j < notes[i].lengthMs / windowLenMs / rollingAverageInputCount; ++j) {
                    Entry e = new Entry((float) (0), curXIndex + maxViewPortSize + j);
                    lineData.getXVals().add("");
                }
                curXIndex += notes[i].lengthMs / windowLenMs / rollingAverageInputCount;
            }
            else {
                ArrayList<Entry> deviationsOfCurNote = new ArrayList<>();
                for (int j = 0; j < notes[i].deviations.size(); ++j) {
                    rollingAverageFinder.write(notes[i].getDeviation(j));
                }

                List<Double> averageDeviations = rollingAverageFinder.read();
                for (int j = 0; j < averageDeviations.size(); ++j) {
                    Entry e = new Entry((float)(double)(averageDeviations.get(j)) / 100, curXIndex + maxViewPortSize + j);
                    deviationsOfCurNote.add(e);
                    if (j == (averageDeviations.size() / 2)) {
                        lineData.getXVals().add(String.valueOf(notes[i].degree));
                    } else {
                        lineData.getXVals().add("");
                    }
                    LineDataSet lds = new LineDataSet(deviationsOfCurNote, "");
                    formatDataSet(lds);
                    existingNotes.add(lds);
                }
                curXIndex += averageDeviations.size();
            }
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
