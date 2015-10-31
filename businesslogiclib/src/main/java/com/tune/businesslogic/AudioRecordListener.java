package com.tune.businesslogic;

import java.util.Observable;

/**
 * Created by mart22n on 22.08.2015.
 */
public abstract class AudioRecordListener extends Observable {
    // constants copied from audioformat.java
    public static final int CHANNEL_IN_FRONT = 0x10;
    public static final int CHANNEL_IN_MONO = CHANNEL_IN_FRONT;
    /** Audio data format: PCM 16 bit per sample. Guaranteed to be supported by devices. */
    public static final int ENCODING_PCM_16BIT = 2;
    public static final int SAMPLE_RATE_STANDARD = 44100;

    /**
     *
     * @param channelConfig - currently is working CHANNEL_IN_FRONT
     * @param audioFormat - currently ENCODING_PCM_16BIT is working
     * It is possible for notifications to be lost if the period is too small.
     */
    public abstract void setAudioRecordOptions(int channelConfig, int audioFormat);

    public abstract void start();

    public abstract void stop();
}
