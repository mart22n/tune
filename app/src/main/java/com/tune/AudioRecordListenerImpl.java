package com.tune;

import android.content.Context;
import android.media.AudioRecord;
import android.util.Log;
import android.util.Pair;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mart22n on 22.08.2015.
 */
class AudioRecordListenerImpl extends java.util.Observable implements
AudioRecord.OnRecordPositionUpdateListener, AudioRecordListener {

    boolean shouldAudioReaderThreadDie;
    private Thread audioReaderThread;
    private AudioRecord audioRecord;
    private CircularBuffer circularBuffer;
    private int channelConf;
    private int audioFmt;
    private int sampleRt;
    private int bufSize;
    private short[] audioSamples;
    private double[] pressureValues;
    private String appName;
    private Lock lock;
    private static final int AUDIO_SAMPLING_RATE = 44100;
    private static final int maxSampleSize = 44100; // Length of sample to analyze.
    private double curNotifyRate = 0.4;
    private static final double minNotifyRate = 0.4; // at least every 0.4 s.


    AudioRecordListenerImpl(Context c) {
        appName = c.getResources().getString(R.string.app_name);
        lock = new ReentrantLock();
    }

    @Override
    public void setAudioRecordOptions(int channelConfig, int audioFormat, int sampleRate) {
        channelConf = channelConfig;
        audioFmt = audioFormat;
        sampleRt = sampleRate;
        getMinBufferSize();
    }

    @Override
    public boolean setPositionNotificationPeriod(int ms) {
        if(audioRecord.setPositionNotificationPeriod(
                (int)(ms*AUDIO_SAMPLING_RATE / 1000)) !=
                AudioRecord.SUCCESS) {
            Log.e(appName, "Invalid notify rate.");
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
     * @param recorder
     */
    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
            if(!lock.tryLock()) {
                Log.e(appName, "Analyzing algorithm is too slow. Dropping sample");
                return;
            }
            double[] pressureValues = new double[4 * bufSize + 100];
            int elementsRead =
                    circularBuffer.getElements(pressureValues, 0, maxSampleSize);
            if(elementsRead == maxSampleSize) {
                Log.e(appName, "Too many samples read from circular buffer");
            }
            notifyObservers(new Pair<double[ ], Integer>(pressureValues, elementsRead));

            lock.unlock();
            }
        }).start();
    }

    /**
     * Start audio recording in separate thread (thread writes samples into ringbuffer)
     */
    void startListening() {
        shouldAudioReaderThreadDie = false;
        audioReaderThread = new Thread(new Runnable() {
            public void run() {
                while(!shouldAudioReaderThreadDie) {
                    int shortsRead = audioRecord.read(audioSamples,0,bufSize);
                    if(shortsRead < 0) {
                        Log.e(appName, "Could not read audio data.");
                    } else {
                        for(int i=0; i<shortsRead; ++i) {
                            circularBuffer.push(audioSamples[i]);
                        }
                    }
                }
                Log.d(appName, "AudioReaderThread reached the end");
            }
        });
        audioReaderThread.setDaemon(false);
        audioReaderThread.start();
    }

    void stopListening() {
        shouldAudioReaderThreadDie = true;
        try {
            audioReaderThread.join();
        } catch(Exception e) {
            Log.e(appName, "Could not join audioReaderThread: " + e.getMessage());
        }
    }

    /**
     * Returns the minimum buffer size required for the successful creation of an AudioRecord
     * object, in byte units.
     * Note that this size doesn't guarantee a smooth recording under load, and higher values
     * should be chosen according to the expected frequency at which the AudioRecord instance
     * will be polled for new data.
     **/
    private int getMinBufferSize() {
        bufSize = AudioRecord.getMinBufferSize(sampleRt,
                channelConf,
                audioFmt)
                * 2;
        pressureValues = new double[4 * bufSize + 100];
        return bufSize;
    }
}
