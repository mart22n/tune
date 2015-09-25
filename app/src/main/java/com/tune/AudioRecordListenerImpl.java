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
public class AudioRecordListenerImpl extends java.util.Observable implements
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
    private String tag;
    private Lock lock;
    private static final int AUDIO_SAMPLING_RATE = 44100;
    private static final int maxSampleSize = 44100; // Length of sample to analyze.
    private double curNotifyRate = 0.4;
    private static final double minNotifyRate = 0.4; // at least every 0.4 s.


    AudioRecordListenerImpl(Context c) {
        tag = c.getResources().getString(R.string.tag);
        lock = new ReentrantLock();
    }

    @Override
    public void setAudioRecordOptions(int channelConfig, int audioFormat, int sampleRate,
                                      int positionNotificationPeriodMs) {
        channelConf = channelConfig;
        audioFmt = audioFormat;
        sampleRt = sampleRate;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig,
                audioFormat, getMinBufferSize());
        setPositionNotificationPeriod(positionNotificationPeriodMs);
    }

    private boolean setPositionNotificationPeriod(int ms) {
        if(audioRecord.setPositionNotificationPeriod(
                (int)(ms*AUDIO_SAMPLING_RATE / 1000)) !=
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
     * @param recorder
     */
    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
            if(!lock.tryLock()) {
                Log.e(tag, "Analyzing algorithm is too slow. Dropping sample");
                return;
            }
            double[] pressureValues = new double[4 * bufSize + 100];
            int elementsRead =
                    circularBuffer.getElements(pressureValues, 0, maxSampleSize);
            if(elementsRead == maxSampleSize) {
                Log.e(tag, "Too many samples read from circular buffer");
            }
            notifyObservers(new Pair<double[ ], Integer>(pressureValues, elementsRead));

            lock.unlock();
            }
        }).start();
    }

    @Override
    public int sampleRate() {
        return sampleRt;
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
                    int shortsRead = audioRecord.read(audioSamples,0,bufSize);
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
