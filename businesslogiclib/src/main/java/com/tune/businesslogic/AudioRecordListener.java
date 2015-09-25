package com.tune.businesslogic;

/**
 * Created by mart22n on 22.08.2015.
 */
public interface AudioRecordListener {
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
     * @param positionNotificationPeriodMs - max period between notifications of new available audio samples.
     * It is possible for notifications to be lost if the period is too small.
     */
    public void setAudioRecordOptions(int channelConfig, int audioFormat, int sampleRate,
                                      int positionNotificationPeriodMs);

    public int sampleRate();

    public void start();

    public void stop();
}
