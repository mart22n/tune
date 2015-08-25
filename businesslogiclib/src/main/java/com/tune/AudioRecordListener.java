package com.tune;

/**
 * Created by mart22n on 22.08.2015.
 */
public interface AudioRecordListener {
    // constants copied from audioformat.java
    public static final int CHANNEL_IN_FRONT = 0x10;
    public static final int CHANNEL_IN_MONO = CHANNEL_IN_FRONT;
    /** Audio data format: PCM 16 bit per sample. Guaranteed to be supported by devices. */
    public static final int ENCODING_PCM_16BIT = 2;

    /**
     *
     * @param channelConfig - currently is working CHANNEL_IN_FRONT
     * @param audioFormat - currently ENCODING_PCM_16BIT is working
     */
    public void setAudioRecordOptions(int channelConfig, int audioFormat);

    /**
     * Set max period between notifications of new available audio samples.
     * It is possible for notifications to be lost if the period is too small.
     * @param ms - period in ms
     * @return
     */
    public int setPositionNotificationPeriod(int ms);

    /**
    * Returns the minimum buffer size required for the successful creation of an AudioRecord
    * object, in byte units.
            * Note that this size doesn't guarantee a smooth recording under load, and higher values
            * should be chosen according to the expected frequency at which the AudioRecord instance
    * will be polled for new data.
     **/
    public int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat);
}
