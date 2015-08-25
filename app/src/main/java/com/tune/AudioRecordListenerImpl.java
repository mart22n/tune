package com.tune;

import android.media.AudioRecord;

/**
 * Created by mart22n on 22.08.2015.
 */
class AudioRecordListenerImpl extends java.util.Observable implements
        AudioRecord.OnRecordPositionUpdateListener, AudioRecordListener {
    @Override
    public void setAudioRecordOptions(int channelConfig, int audioFormat) {
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
     * Here observers are notified of new available data from AudioRecord. Data is pulled from
     * fifo and given to observers, using notifyObservers().
     * @param recorder
     */
    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        throw new UnsupportedOperationException();
    }

    private AudioRecord audioRecord;
}
