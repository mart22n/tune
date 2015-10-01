package com.tune;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.tune.businesslogic.AudioRecordListener;
import com.tune.businesslogic.BusinessLogicAdapter;
import com.tune.businesslogic.BusinessLogicAdapterListener;
import com.tune.businesslogic.FrequencyExtractor;
import com.tune.businesslogic.Note;
import com.tune.businesslogic.NoteAndDeviationIdentifier;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer, BusinessLogicAdapterListener {

    public static final String tag = "tune";
    private TextView mainMessage = null;
    private double frequency;
    private SpeedometerGauge gauge;
    LineChart lineChart;
    ImageButton stopStartButton;
    BusinessLogicAdapter businessLogicAdapter;
    AudioRecordListener audioRecordListener;
    private boolean listening;
    private int positionNotificationPeriodMs; //TODO: into settings
    private ChartController chartController;
    FrequencyExtractor.FrequencyExtractorSettings FESettings;
    NoteAndDeviationIdentifier.NoteIdentifierSettings NDISettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gauge = (SpeedometerGauge)findViewById(R.id.speedometer);
        lineChart = (LineChart)findViewById(R.id.chart);
        mainMessage = (TextView)findViewById(R.id.mainMessage);
        FESettings = new FrequencyExtractor.FrequencyExtractorSettings();
        createChart();
        createGauge();
        positionNotificationPeriodMs = 400;
        audioRecordListener = new AudioRecordListenerImpl(this.getApplicationContext());
        try {
            audioRecordListener.setAudioRecordOptions(AudioRecordListener.CHANNEL_IN_FRONT,
                    AudioRecordListener.ENCODING_PCM_16BIT, AudioRecordListener.SAMPLE_RATE_STANDARD,
                    positionNotificationPeriodMs);
        }
        catch(Exception e) {
            Toast.makeText(this, "The are problems with your microphone, app won't work.", Toast.LENGTH_LONG).show();
            Log.e(tag, "Exception when audioRecordListener.setAudioRecordOptions: " + e.getMessage());
        }
        businessLogicAdapter = new BusinessLogicAdapter(audioRecordListener, this);
        businessLogicAdapter.addObserver(this);
        chartController = new ChartController();
        stopStartButton = (ImageButton) findViewById(R.id.startStopButton);
        stopStartButton.setImageResource(R.drawable.stop);
        listening = true;
        businessLogicAdapter.startListeningTune();
        stopStartButton.setOnClickListener(new ImageButton.OnClickListener() {
                                               public void onClick(View v) {
                                                   try {
                                                       if (listening == true) {
                                                           stopStartButton.setImageResource(R.drawable.mike_enabled);
                                                           listening = false;
                                                           findViewById(R.id.action_settings).setEnabled(true);
                                                           businessLogicAdapter.stopListening();
                                                       } else {
                                                           stopStartButton.setImageResource(R.drawable.stop);
                                                           listening = true;
                                                           SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                                           double diff = 0.1;//Double.parseDouble(sharedPref.getString(getString(R.string.pref_default_max_diff_in_percent), "0.1"));
                                                           Toast.makeText(MainActivity.this, Double.toString(diff), Toast.LENGTH_SHORT).show();
                                                           setBLASettingsAndStartIt(diff);
                                                           findViewById(R.id.action_settings).setEnabled(false);
                                                       }
                                                   } catch (Exception e) {
                                                       Log.i(tag, "Error:" + e.getMessage());
                                                   }
                                               }
                                           }
        );

    }

    private void setBLASettingsAndStartIt(double diff) {
        FESettings.maxDiffInPercent = diff;
        businessLogicAdapter.setFrequencyExtractorOptions(FESettings);
        NDISettings = new NoteAndDeviationIdentifier.NoteIdentifierSettings();
        NDISettings.deviationWhereBorderLineStarts = 40;
        NDISettings.measurementWindowMs = 30;
        NDISettings.minNoteLenMs = 100;
        NDISettings.octaveSpan = 2;
        businessLogicAdapter.setNoteIdentifierOptions(NDISettings);
        businessLogicAdapter.startListeningTune();
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
        lineChart.getLegend().setEnabled(false);

        lineChart.getXAxis().setEnabled(true);
        lineChart.getXAxis().setDrawAxisLine(true);
/*        barChart.getXAxis().setDrawGridLines(true);
        barChart.getXAxis().setGridColor(Color.WHITE);*/
        lineChart.getXAxis().setAxisLineColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        // define where to add labels of x-axis
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
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
        lineChart.invalidate(); // refresh
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            try {
                Intent in = new Intent(getApplicationContext(), SettingsActivity.class);
                //in.putExtra("FrequencyExtractorSettings", FESettings);
                startActivity(in);
            } catch (Exception e) {
                Log.i(tag, "Failed to launch SettingsActivity: " + e.getMessage());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(tag, "onDestroy()");
        if(businessLogicAdapter != null)
            businessLogicAdapter.stopListening();
    }

    @Override
    /**called when another activity comes in front of the activity*/
    protected void onPause() {
        super.onPause();
        Log.d(tag, "onPause()");
        listening = false;
        stopStartButton.setImageResource(R.drawable.mike_enabled);
        if(businessLogicAdapter != null)
            businessLogicAdapter.stopListening();
    }

    @Override
    /**called when the activity comes back to the foreground after user has pressed e.g. home button and has restarted app thereafter*/
    protected void onRestart() {
        super.onRestart();
        Log.d(tag,"onRestart()");
        stopStartButton.setImageResource(R.drawable.mike_enabled);
        listening = false;
    }

    @Override
    /**called when activity will start interacting with the user.*/
    protected void onResume() {
        super.onResume();
        Log.d(tag,"onResume()");
    }

    @Override
    /**called when activity is becoming visible to the user.*/
    protected void onStart() {
        super.onStart();
        Log.d(tag, "onStart()");
    }

    @Override
    /**user has pressed e.g. home button*/
    protected void onStop() {
        super.onStop();
        Log.d(tag, "onStop()");
        listening = false;
        if(businessLogicAdapter != null)
            businessLogicAdapter.stopListening();
    }

    @Override
    public void update(Observable who, Object obj) {
    }

    @Override
    public void onFirstNoteDetected(Note note) {

    }

    @Override
    public void onNewNotesOrPausesAvailable(Note[] notes) {
        chartController.drawNotes(notes);
    }

    @Override
    public void onToastNotification(String notification) {
        Toast.makeText(this, notification, Toast.LENGTH_LONG).show();
    }
}
