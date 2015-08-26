package com.tune;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;

//import com.github.mikephil.charting.data.BarData;

//import com.github.mikephil.charting.charts.BarChart;


class MainActivity extends Activity implements Observer, BusinessLogicAdapterListener {

    public static final String TAG = "Tune";
    private SoundAnalyzer soundAnalyzer = null ;
    private TextView mainMessage = null;
    private double frequency;
    private SpeedometerGauge gauge;
   // BarChart barChart;
    LineChart lineChart;
    BusinessLogicAdapter businessLogicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            soundAnalyzer = new SoundAnalyzer();
            soundAnalyzer.addObserver(this);
        } catch(Exception e) {
            Toast.makeText(this, "The are problems with your microphone, app won't work.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception when instantiating SoundAnalyzer: " + e.getMessage());
        }
        mainMessage = (TextView)findViewById(R.id.mainMessage);
        gauge = (SpeedometerGauge)findViewById(R.id.speedometer);
        //barChart = (BarChart) findViewById(R.id.chart);
        lineChart = (LineChart)findViewById(R.id.chart);

        createChart();
        createGauge();


    }

    private void createGauge() {
        // Add label converter
        gauge.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        gauge.setMaxSpeed(300);
        gauge.setMajorTickStep(30);
        gauge.setMinorTicks(2);

        // Configure value range colors
        gauge.addColoredRange(30, 140, Color.GREEN);
        gauge.addColoredRange(140, 180, Color.YELLOW);
        gauge.addColoredRange(180, 400, Color.RED);
    }

    private void createChart() {

        lineChart.setBackgroundColor(Color.BLACK);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(false);
        lineChart.setDragDecelerationFrictionCoef(0.85f);
        lineChart.setMinimumHeight(500);
        lineChart.setDescription("");

        lineChart.getXAxis().setEnabled(true);
        lineChart.getXAxis().setDrawAxisLine(true);
/*        barChart.getXAxis().setDrawGridLines(true);
        barChart.getXAxis().setGridColor(Color.WHITE);*/
        lineChart.getXAxis().setAxisLineColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        // define where to add labels of x-axis
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        LimitLine ll = new LimitLine(2f, "150");
        ll.setLineColor(Color.YELLOW);
        ll.setLineWidth(4f);
        lineChart.getXAxis().addLimitLine(ll);

        lineChart.getAxisLeft().setAxisMaxValue(60);    // max deviation = +/-50 cents
        lineChart.getAxisLeft().setAxisMinValue(-60);
        lineChart.getAxisLeft().setStartAtZero(false);

        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisLeft().setDrawAxisLine(true);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setAxisLineColor(Color.WHITE);

        ArrayList<Entry> note1 = new ArrayList<Entry>();
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
        lineChart.invalidate(); // refresh


        /*barChart.setBackgroundColor(Color.BLACK);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleXEnabled(true);
        barChart.setScaleYEnabled(false);
        barChart.setDragDecelerationFrictionCoef(0.85f);
        barChart.setMinimumHeight(500);
        barChart.setBackgroundColor(Color.GRAY);    //TODO: was black
        barChart.setDescription("");
        barChart.setDrawValueAboveBar(false);

        barChart.getXAxis().setEnabled(true);
        barChart.getXAxis().setDrawAxisLine(true);
*//*        barChart.getXAxis().setDrawGridLines(true);
        barChart.getXAxis().setGridColor(Color.WHITE);*//*
        barChart.getXAxis().setAxisLineColor(Color.WHITE);
        barChart.getXAxis().setTextColor(Color.WHITE);
        // define where to add labels of x-axis
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        LimitLine ll = new LimitLine(2f, "150");
        ll.setLineColor(Color.YELLOW);
        ll.setLineWidth(4f);
        barChart.getXAxis().addLimitLine(ll);

        barChart.getAxisLeft().setAxisMaxValue(60);    // max deviation = +/-50 cents
        barChart.getAxisLeft().setAxisMinValue(-60);
        barChart.getAxisLeft().setStartAtZero(false);

        barChart.getAxisLeft().setEnabled(true);
        barChart.getAxisLeft().setDrawAxisLine(true);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setAxisLineColor(Color.WHITE);

        List<BarEntry> barValues = new ArrayList<BarEntry>();
        barValues.add(new BarEntry(20f, 0));
        barValues.add(new BarEntry(30f, 1));
        barValues.add(new BarEntry(0f, 3));
        barValues.add(new BarEntry(-30f, 2));
        barValues.add(new BarEntry(-1f, 3));
        BarDataSet dataSet = new BarDataSet(barValues, "");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setBarSpacePercent(0);
        dataSet.setColor(Color.WHITE);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(dataSet);

        // x-values
        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("C");
        xVals.add("D");
        xVals.add("E");
        xVals.add("F");
        xVals.add("G");

        BarData chartData = new BarData(xVals, dataSets);

        barChart.setData(chartData);

        // chart is redrawn
        barChart.invalidate();*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_measuring, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart()");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        if(soundAnalyzer!=null)
            soundAnalyzer.ensureStarted();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart()");
        if(soundAnalyzer!=null)
            soundAnalyzer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop()");
        if(soundAnalyzer!=null)
            soundAnalyzer.stop();
    }

    @Override
    public void update(Observable who, Object obj) {
            //if(obj instanceof SoundAnalyzer.AnalyzedSound) {
            SoundAnalyzer.AnalyzedSound result = (SoundAnalyzer.AnalyzedSound)obj;
            // result.getDebug();
            frequency = result.frequency;//FrequencySmoothener.getSmoothFrequency(result);
            if(result.error== SoundAnalyzer.AnalyzedSound.ReadingType.BIG_FREQUENCY) {
                mainMessage.setTextColor(Color.rgb(255,36,0));
                mainMessage.setText("Too high frequency");
                Log.e(TAG, "Too high frequency");
            }
            else if(result.error== SoundAnalyzer.AnalyzedSound.ReadingType.BIG_VARIANCE) {
                mainMessage.setTextColor(Color.rgb(255,36,0));
                mainMessage.setText("Too big variance");
                Log.e(TAG, "Too big variance");
            }
            else if(result.error== SoundAnalyzer.AnalyzedSound.ReadingType.ZERO_SAMPLES) {
                mainMessage.setTextColor(Color.rgb(255,36,0));
                mainMessage.setText("Less than 2 waves");
                Log.e(TAG, "Less than 2 waves");
            }
            else if(result.error== SoundAnalyzer.AnalyzedSound.ReadingType.TOO_QUIET) {
                mainMessage.setTextColor(Color.rgb(255,36,0));
                mainMessage.setText("Too quiet");
                Log.e(TAG, "Too quiet");
            }
            else if(result.error== SoundAnalyzer.AnalyzedSound.ReadingType.NO_PROBLEMS)
            {
                mainMessage.setTextColor(Color.rgb(34,139,34));
                mainMessage.setText("Frequency: " + frequency);
                gauge.setSpeed(frequency);
                Log.e(TAG, "Sample OK, freq = " + frequency);
            }
            else {
                Log.e(TAG, "UiController: Unknown class of message.");
            }
            if(ConfigFlags.uiControlerInformsWhatItKnowsAboutSound)
                result.getDebug();
            //Log.e(TAG,"Frequency: " + frequency);
            /*} else if(obj instanceof SoundAnalyzer.ArrayToDump) {
                SoundAnalyzer.ArrayToDump a = (SoundAnalyzer.ArrayToDump)obj;
                ui.dumpArray(a.arr, a.elements);
            }*/
    }

    @Override
    public void onFirstNoteDetected(Note note) {

    }

    @Override
    public void onNewNotesOrPausesAvailable(List<Note> notes) {

    }
}
