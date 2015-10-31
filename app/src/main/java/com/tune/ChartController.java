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
    private WindowManager windowManager;
    private int chartWidthCm;
    private ArrayList<ArrayList<Integer>> addableNotes = new ArrayList<>();
    private int noteIndex = 0;

    public ChartController(int rollingSpeedCmPerSecond, int minNoteLenMs, LineChart lineChart, WindowManager windowManager) {
        this.rollingSpeedCmPerSecond = rollingSpeedCmPerSecond;
        this.minNoteLenMs = minNoteLenMs;
        this.lineChart = lineChart;
        this.windowManager = windowManager;
        for(int i = 0; i < 1000; i += 1) {
            ArrayList<Integer> note = new ArrayList<>();
            note.add(i % 3 > 0 ? 15 : -15);
            note.add(i % 3 > 0 ? 25 : -25);
            note.add(i % 3 > 0 ? 5 : -5);
            addableNotes.add(note);
        }
    }

    private void setRealDeviceSizeInPixels() {
        Display display = windowManager.getDefaultDisplay();
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
        windowManager.getDefaultDisplay().getMetrics(dm);
        chartWidthCm = (int)(mWidthPixels/dm.xdpi * 2.54);

        lineChart.setBackgroundColor(Color.BLACK);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(false);
        lineChart.setDragDecelerationFrictionCoef(0.85f);
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
        lineChart.getXAxis().setDrawGridLines(true);
        lineChart.getXAxis().setGridColor(Color.WHITE);
/*        LimitLine ll = new LimitLine(2f, "150");
    ll.setLineColor(Color.YELLOW);
    ll.setLineWidth(4f);
    lineChart.getXAxis().addLimitLine(ll);*/

        lineChart.getAxisLeft().setAxisMaxValue(55);    // max deviation = +/-50 cents
        lineChart.getAxisLeft().setAxisMinValue(-55);
        lineChart.getAxisLeft().setStartAtZero(false);

        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisLeft().setDrawAxisLine(true);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setAxisLineColor(Color.WHITE);
        lineChart.getAxisLeft().setGridColor(Color.WHITE);




        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        LineDataSet dsNote1 = new LineDataSet(new ArrayList<Entry>(), "");

        for(int i = 0; i < 18; ++i) {
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
        lds.setValueTextSize(16f);
        lds.setValueTypeface(Typeface.create((String)null, Typeface.BOLD));
    }

    void drawNotes(Note[] notes) {
        LineData lineData = lineChart.getData();

        List<LineDataSet> existingNotes = lineData.getDataSets();

        ArrayList<Entry> displayedDeviations = new ArrayList<>();
        if(noteIndex < 1000) {
            for (int j = 0; j < 3; ++j) {
                Entry e = new Entry(addableNotes.get(noteIndex).get(j), noteIndex * 3 + 18 + j);
                displayedDeviations.add(e);
                if(j == 1)
                    lineData.getXVals().add(Integer.toString(noteIndex));
                else
                    lineData.getXVals().add("");
            }

            ++noteIndex;

            LineDataSet lds = new LineDataSet(displayedDeviations, "");
            formatDataSet(lds);
            existingNotes.add(lds);
        }
        /*LineData lineData = lineChart.getData();
        List<LineDataSet> existingNotes = lineData.getDataSets();
        ArrayList<Entry> displayedDeviations = new ArrayList<>();
        for (int i = 0; i < notes.length; ++i) {
            // shift current notes to the left by the width of new note
            for(int j = 0; j < existingNotes.size(); ++j) {
                for (int l = 0; l < existingNotes.get(j).getYVals().size(); ++l) {
                    for (int k = 0; k < notes[i].deviations.size(); ++k) {
                        Entry e = existingNotes.get(j).getYVals().get(l);
                        if (e == null) continue;

                        e.setXIndex(e.getXIndex() - 1);
                    }
                }
            }

           *//* // place the new note
            displayedDeviations = new ArrayList<>();
            for(int j = 0; j < notes[0].deviations.size(); ++j) {
                Entry e = new Entry(notes[0].deviations.get(j), j);
                displayedDeviations.add(e);
            }
*//*
        }*/

        /*LineDataSet lds = new LineDataSet(displayedDeviations, "");
        formatDataSet(lds);
        existingNotes.add(lds);*/
       /* for (int i = 0; i < 1; ++i) {
            int startIndex = existingNotes.get(0).getEntryCount() - existingNotes.get(1).getYVals().size();
            for (int j = startIndex; j < startIndex + existingNotes.get(1).getEntryCount(); ++j) {
                existingNotes.get(1).getEntryForXIndex(j).setVal(notes[i].deviations.get(j - startIndex));
            }
        }
*/
        //lineChart.setScaleMinima(existingNotes.size() / 6, 1);
        lineChart.setVisibleXRange(18, 18);

        lineChart.centerViewTo(lineData.getXValCount() - 9, 0, YAxis.AxisDependency.RIGHT);

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
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
