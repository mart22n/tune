package com.tune;

import android.content.Context;
import android.media.AudioRecord;
import android.util.Log;
import android.util.Pair;

import com.tune.businesslogic.AudioRecordListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mart22n on 12.10.2015.
 */
public class AudioRecordListenerImplForTesting extends AudioRecordListener {

    boolean shouldAudioReaderThreadDie;
    private Thread audioReaderThread;
    private AudioRecord audioRecord;
    private CircularBuffer circularBuffer;
    private int channelConf;
    private int audioFmt;
    private int sampleRt;
    private short[] audioSamples;
    private double[] pressureValues;
    private String tag;
    private Lock lock;
    private static final int AUDIO_SAMPLING_RATE = 44100;
    private static final int maxSampleSize = 44100; // Length of sample to analyze.
    private static final int notificationPeriod = 400;
    private static final int sampleLen = AUDIO_SAMPLING_RATE * notificationPeriod / 1000;


    AudioRecordListenerImplForTesting(Context c) {
        tag = c.getResources().getString(R.string.tag);
        circularBuffer = new CircularBuffer(sampleLen);
        pressureValues = new double[4 * sampleLen + 100];
        lock = new ReentrantLock();
    }

    @Override
    public void setAudioRecordOptions(int channelConfig, int audioFormat) {
        audioSamples = new short[sampleLen];
    }

    /**
     * Start audio recording in separate thread (thread writes samples into ringbuffer)
     */
    @Override
    public void start() {
        shouldAudioReaderThreadDie = false;
        audioReaderThread = new Thread(new Runnable() {
            public void run() {
                while(!shouldAudioReaderThreadDie) {
                    int shortsRead = 0;
                    double[] buf = new double[1];
                    try {
                     /*   buf = readAudioFile();
                        Thread.sleep(400);*/
                        setChanged();
                        notifyObservers(new Pair<>(buf, buf.length));
                    }
                    catch (Exception e) {
                        Log.e(tag, "Exception in filereader thread: " + e.getMessage());
                    }
                    if(buf.length < 0) {
                        Log.e(tag, "Could not read audio data.");
                    }
                }
                Log.d(tag, "AudioReaderThread reached the end");
            }
        });
        audioReaderThread.setDaemon(false);
        audioReaderThread.start();
    }

    @Override
    public void stop() {
        shouldAudioReaderThreadDie = true;
        try {
            audioReaderThread.join();
        } catch(Exception e) {
            Log.e(tag, "Could not join audioReaderThread: " + e.getMessage());
        }
        //audioRecord.stop();
    }

    private double[] readAudioFile() {
        double[] result = new double[1000000];
        try {
            //File file = new File(getClass().getResource("/audiosamples.txt").getFile());
            //Assert.assertEquals(true, file.exists());
            File file = new File("/storage/emulated/0/Android/data/com.tune/files", "audiosamples.txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fileInputStream, Charset.forName("ASCII"));

            BufferedReader br = new BufferedReader(isr);
            String line = "";
            int i = 0;
            while((line = br.readLine()) != null) {
                result[i++] = Double.parseDouble(line);
            }
        }
        catch (IOException io) {
            int i = 5;
            int a = i;
        }
        return result;
    }

    /**
     * Returns the minimum buffer size required for the successful creation of an AudioRecord
     * object, in byte units.
     * Note that this size doesn't guarantee a smooth recording under load, and higher values
     * should be chosen according to the expected frequency at which the AudioRecord instance
     * will be polled for new data.
     **/
    private int getMinBufferSize() {
        int bufSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLING_RATE,
                channelConf,
                audioFmt)
                * 2;
        return bufSize;
    }
}
