package com.tune;

import android.media.AudioRecord;

/**
 * Created by mart22n on 22.08.2015.
 */
class AudioRecordListenerImpl extends java.util.Observable implements
AudioRecord.OnRecordPositionUpdateListener, AudioRecordListener {

    boolean shouldAudioReaderThreadDie;
    Thread audioReaderThread;

    @Override
    public void setAudioRecordOptions(int channelConfig, int audioFormat, int sampleRate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int setPositionNotificationPeriod(int ms) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
        throw new UnsupportedOperationException();
    }

    /**
     * Here observers are notified of new available data from AudioRecord. Data is pulled as an array
     * of doubles from circularBuffer and given to observers, using notifyObservers().
     * @param recorder
     */
    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        throw new UnsupportedOperationException();
    }

    /**
     * Start audio recording in separate thread (thread writes samples into ringbuffer)
     */
    void startListening() {
        shouldAudioReaderThreadDie = false;
        audioReaderThread = new Thread(new Runnable() {
            public void run() {
             /*   while(!shouldAudioReaderThreadDie) {
                    int shortsRead = audioRecord.read(audioDataTemp,0,audioDataSize);
                    if(shortsRead < 0) {
                        Log.e(TAG, "Could not read audio data.");
                    } else {
                        for(int i=0; i<shortsRead; ++i) {
                            audioData.push(audioDataTemp[i]);
                        }
                    }
                }
                Log.d(TAG, "AudioReaderThread reached the end");*/
            }
        });
        audioReaderThread.setDaemon(false);
        audioReaderThread.start();
    }

    void stopListening() {
        throw new UnsupportedOperationException();
    }


    private AudioRecord audioRecord;
    private CircularBuffer circularBuffer;
}
