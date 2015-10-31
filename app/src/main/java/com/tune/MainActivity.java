package com.tune;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
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
    private double frequency;
    private SpeedometerGauge gauge;
    LineChart lineChart;
    ImageButton stopStartButton;
    BusinessLogicAdapter businessLogicAdapter;
    AudioRecordListener audioRecordListener;
    private boolean listening;
    private ChartController chartController;
    FrequencyExtractor.FrequencyExtractorSettings FESettings;
    NoteAndDeviationIdentifier.NoteIdentifierSettings NDISettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gauge = (SpeedometerGauge)findViewById(R.id.speedometer);
        lineChart = (LineChart)findViewById(R.id.chart);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup vg = (ViewGroup)(gauge.getParent());
            vg.removeView(gauge);
        }

        FESettings = new FrequencyExtractor.FrequencyExtractorSettings();
        createGauge();
        audioRecordListener = new AudioRecordListenerImplForTesting(this.getApplicationContext());
        try {
            audioRecordListener.setAudioRecordOptions(AudioRecordListener.CHANNEL_IN_FRONT,
                    AudioRecordListener.ENCODING_PCM_16BIT);
        }
        catch(Exception e) {
            Toast.makeText(this, "The are problems with your microphone, app won't work.", Toast.LENGTH_LONG).show();
            Log.e(tag, "Exception when audioRecordListener.setAudioRecordOptions: " + e.getMessage());
        }
        businessLogicAdapter = new BusinessLogicAdapter(audioRecordListener, this);
        businessLogicAdapter.addObserver(this);
        chartController = new ChartController(1, 100, lineChart, getWindowManager());
        chartController.initChart(this.getResources().getConfiguration().orientation);
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
    public void onNewNotesOrPausesAvailable(final Note[] notes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chartController.drawNotes(notes);
            }
        });
    }

    @Override
    public void onToastNotification(String notification) {
        Toast.makeText(this, notification, Toast.LENGTH_LONG).show();
    }

 /*   @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup vg = (ViewGroup)(gauge.getParent());
            vg.removeView(gauge);
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }*/
}
