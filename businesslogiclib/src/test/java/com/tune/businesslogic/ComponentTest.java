package com.tune.businesslogic;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by mart22n on 2.10.2015.
 */
public class ComponentTest extends TestBase {

    @Before
    public void setUp() {
        int sampleRate = 1000;
        int loudnessThreshold = 0;
        int nofConsecutiveCrossingsToMeasure = 4;
        int maxDiffInPercent = 1;
        int measurementWindowMs = 30;
        int gapBetweenSamplesWhenDetectingPause = 1;
        setFESettings(sampleRate, loudnessThreshold,
                nofConsecutiveCrossingsToMeasure, measurementWindowMs, maxDiffInPercent,
                gapBetweenSamplesWhenDetectingPause);

        int minNoteLenMs = 60;
        int octaveSpan = 2;
        int deviationWhereBorderlineStarts = 45;
        setNISettings(measurementWindowMs, minNoteLenMs, octaveSpan, deviationWhereBorderlineStarts);
    }

    @Test
    public void whenTwoLongEnoughFreqsInputted_noteWithfirstFreqIsOutputted() {
        double[] input = {0, 1, 0, -1, 0, 1, 0, -1,// 250 hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,

                0, 1, 1, 0, -1, 0, 1, 1, 0, -1, // 200 hz
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1
        };

        double[] windows = new double[] { 250.0, 250.0, 250.0, 200.0, 200.0, 200};//frequencyExtractor.extractFrequencies(input, input, input.lengthMs);
        Note[] output = noteAndDeviationIdentifier.convertWaveformToNotes(windows);
        Assert.assertEquals(1, output.length);
    }

    @Test
    public void whenNoteTooShort_itIsIgnored() {
        double[] input = {0, 1, 0, -1, 0, 1, 0, -1,// 250 hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,

                0, 1, 1, 0, -1, 0, 1, 1, 0, -1, // 200 hz
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1
        };

        double[] windows = frequencyExtractor.extractFrequencies(input, input, input.length);
        Note[] output = noteAndDeviationIdentifier.convertWaveformToNotes(windows);
        Assert.assertEquals(0, output.length);
    }

    @Test
    public void whenFreqsExtractedMultipleTimes_correctFreqsAreOutputted() {
        double[] input = {0, 1, 0, -1, 0, 1, 0, -1,// 250 hz
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,
                0, 1, 0, -1, 0, 1, 0, -1,

                0, 1, 1, 0, -1, 0, 1, 1, 0, -1, // 200 hz
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1
        };

        double[] windows = frequencyExtractor.extractFrequencies(input, input, input.length);
        Assert.assertEquals(250.0, windows[0]);
        Assert.assertEquals(5, windows.length);
        Assert.assertEquals(10, frequencyExtractor.nofSamplesInPrevInputsLastWindow);

        double[] input2 = {0, 1, 1, 1, 0, -1, -1, -1, // 125Hz
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,
                0, 1, 1, 1, 0, -1, -1, -1,

                0, 1, 1, 0, -1, 0, 1, 1, 0, -1, // 200 hz
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1,
                0, 1, 1, 0, -1, 0, 1, 1, 0, -1
        };

        windows = frequencyExtractor.extractFrequencies(input2, input2, input2.length);
        Assert.assertEquals(-1.0, windows[0]);
        Assert.assertEquals(125.0, windows[1]);
        Assert.assertEquals(200.0, windows[3]);
        Assert.assertEquals(12, frequencyExtractor.nofSamplesInPrevInputsLastWindow);
    }

    /**
     * ca 28000 samples, 3 notes
     */
    @Test
    public void whenSamplesAreReadFromFile_outputIscorrect() {
        setFESettings(44100, 0, 4, 30, 1, 100);
        setNISettings(30, 100, 2, 45);
        double[] samples = readAudioFile();
        double[] windows = frequencyExtractor.extractFrequencies(samples, samples, samples.length);
        Note[] notes = noteAndDeviationIdentifier.convertWaveformToNotes(windows);
    }

    @Rule
    public ResourceFile res = new ResourceFile("audiosamples.txt");

    private double[] readAudioFile() {
        double[] result = new double[1];
        try {
            //File file = new File(getClass().getResource("/audiosamples.txt").getFile());
            //Assert.assertEquals(true, file.exists());
            //result = readAudioFileAsHumanReadable();
            result = readAudioFileAsBinary();
        }
        catch (IOException io) {

        }
        return result;
    }

    private double[] readAudioFileAsBinary() throws IOException {
        byte[] contents = new byte[(int)res.getContent().length()];
        assert(res.getInputStream().read(contents) == contents.length);
        res.getInputStream().close();
        java.io.ByteArrayInputStream ba = new java.io.ByteArrayInputStream(contents);
        java.io.DataInputStream d = new java.io.DataInputStream(ba);
        double num = 0;
        double[] res = new double[contents.length / 8];
        try {
            for(int i = 0; i < contents.length; ++i) {
                res[i] = d.readDouble();
            }
        }
        catch (EOFException eof) { }
        return res;
    }

    private double[] readAudioFileAsHumanReadable() throws IOException {
        String contents = res.getContent();
        Assert.assertEquals(true, contents.length() > 0);
        String[] parts = contents.split("\r\n");
        double[] result = new double[parts.length];
        for(int i = 0; i < parts.length; ++i) {
            result[i] = Double.parseDouble(parts[i]);
        }
        return result;
    }

    class ResourceFile extends ExternalResource {
        String res;
        File file = null;

        InputStream stream;

        public ResourceFile(String res) {
            this.res = res;
        }

        public File getFile() throws IOException {
            return file;
        }

        public InputStream getInputStream() {
            return stream;
        }

        public InputStream createInputStream() {
            return getClass().getResourceAsStream(res);
        }

        public String getContent() throws IOException {
            return getContent("ASCII");
        }

        public String getContent(String charSet) throws IOException {
            InputStreamReader reader = new InputStreamReader(createInputStream(),
                    Charset.forName(charSet));
            char[] tmp = new char[409600];
            StringBuilder b = new StringBuilder();
            try {
                while (true) {
                    int len = reader.read(tmp);
                    if (len < 0) {
                        break;
                    }
                    b.append(tmp, 0, len);
                }
                reader.close();
            } finally {
                reader.close();
            }
            return b.toString();
        }

        @Override
        protected void before() throws Throwable {
            super.before();
            stream = getClass().getResourceAsStream(res);
        }

        @Override
        protected void after() {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
            if (file != null) {
                file.delete();
            }
            super.after();
        }
    }
}
