package com.tune.businesslogic;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Created by mart22n on 26.08.2015.
 */
public class HarmonicsRemover {

    private int currentFftMethodSize = -1;
    private DoubleFFT_1D fft_method;
    private int sampleLen = 7200;
    private double sq(double a) { return a*a; };

    private double hanning(int n, int N) {
        return 0.5*(1.0 -Math.cos(2*Math.PI*(double)n/(double)(N-1)));
    }

    HarmonicsRemover() {
        fft_method = new DoubleFFT_1D(sampleLen);
    }

    double[] removeHarmonics(double[] sample, int sampleSize) {
        // Fourier Transform to calculate autocorrelation. This kind of magic,
        // but it works :) Also some stupid people confuse definition of forward
        // transforms with inverse transforms. But the same people write multi-
        // threaded apps for computing them, so we kind of like them :)

        // Below i use circular convolution theorem.

        // Should save some memory.
        if(2*sampleSize != currentFftMethodSize) {
            fft_method = new DoubleFFT_1D(2*sampleSize);
            currentFftMethodSize = 2*sampleSize;
        }

        // Check out memory layout of fft methods in Jtransforms.
        for(int i=sampleSize-1; i>=0; i--) {
            sample[2*i]=sample[i] * hanning(i,sampleSize);
            sample[2*i+1] = 0;
        }
        for(int i=2*sampleSize; i<sample.length; ++i)
            sample[i]=0;

        // Compute FORWARD fft transform.
        fft_method.complexInverse(sample, false);

        // Replace every frequency with it's magnitude.
        for(int i=0; i<sampleSize; ++i) {
            sample[2*i] = sq(sample[2*i]) + sq(sample[2*i+1]);
            sample[2*i+1] = 0;
        }
        for(int i=2*sampleSize; i<sample.length; ++i)
            sample[i]=0;

        // Set first one on to 0.
        sample[0] = 0;

        // Compute INVERSE fft.
        fft_method.complexForward(sample);

        // Take real part of the result.
        for(int i=0; i<sampleSize; ++i)
            sample[i] = sample[2*i];
        for(int i=sampleSize; i<sample.length; ++i)
            sample[i]=0;
        return sample;
    }
}
