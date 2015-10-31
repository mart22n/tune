package com.tune;

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.Pair;

import com.tune.businesslogic.AudioRecordListener;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mart22n on 22.08.2015.
 */
public class AudioRecordListenerImpl extends AudioRecordListener implements
AudioRecord.OnRecordPositionUpdateListener {

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


    AudioRecordListenerImpl(Context c) {
        tag = c.getResources().getString(R.string.tag);
        circularBuffer = new CircularBuffer(sampleLen);
        pressureValues = new double[4 * sampleLen + 100];
        lock = new ReentrantLock();
    }

    @Override
    public void setAudioRecordOptions(int channelConfig, int audioFormat) {
        channelConf = channelConfig;
        audioFmt = audioFormat;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLING_RATE, channelConfig,
                audioFormat, getMinBufferSize());
        audioRecord.setRecordPositionUpdateListener(this);
        setPositionNotificationPeriod(notificationPeriod);
        if(audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(tag, "Could not initialize microphone.");
        }
        audioSamples = new short[sampleLen];
    }

    private boolean setPositionNotificationPeriod(int ms) {
        if(audioRecord.setPositionNotificationPeriod(
                (int) (ms * AUDIO_SAMPLING_RATE / 1000)) !=
                AudioRecord.SUCCESS) {
            Log.e(tag, "Invalid notify rate.");
            return false;
        }
        return true;
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
    }

    /**
     * Here observers are notified of new available data from AudioRecord. Data is pulled as an array
     * of doubles from circularBuffer and given to observers, using notifyObservers().
     */
    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
            if(!lock.tryLock()) {
                //Log.e(tag, "Analyzing algorithm is too slow. Dropping sample"); //TODO: uncomment
                return;
            }
            int elementsRead =
                    circularBuffer.getElements(pressureValues, 0, maxSampleSize);
            if(elementsRead == maxSampleSize) {
                Log.e(tag, "Too many samples read from circular buffer");
            }
            setChanged();
            notifyObservers(new Pair<double[], Integer>(pressureValues, elementsRead));

            lock.unlock();
            }
        }).start();
    }

    /**
     * Start audio recording in separate thread (thread writes samples into ringbuffer)
     */
    @Override
    public void start() {
        audioRecord.startRecording();
        shouldAudioReaderThreadDie = false;
        audioReaderThread = new Thread(new Runnable() {
            public void run() {
                while(!shouldAudioReaderThreadDie) {
                    int shortsRead = 0;
                    try {
                        shortsRead = audioRecord.read(audioSamples,0, sampleLen);
                    }
                    catch (Exception e) {
                        Log.e(tag, "Exception in audiorecord.read(): " + e.getMessage());
                    }
                    if(shortsRead < 0) {
                        Log.e(tag, "Could not read audio data.");
                    } else {
                        for(int i=0; i<shortsRead; ++i) {
                            circularBuffer.push(audioSamples[i]);
                        }
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
        audioRecord.stop();
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
