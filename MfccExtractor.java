package com.example.voice;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;

public class MfccExtractor {
    private final int sampleRate;
    private final int bufferSize;
    private final int hopSize;
    private final MFCC mfcc;

    public MfccExtractor(int sampleRate) {
        this.sampleRate = sampleRate;
        this.bufferSize = 512;
        this.hopSize = 256;
        this.mfcc = new MFCC(bufferSize, sampleRate, 13, 40, 300, sampleRate / 2);
    }

    public float[] compute(float[] audio) {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFloatArray(audio, sampleRate, bufferSize, hopSize);
        dispatcher.addAudioProcessor(mfcc);
        dispatcher.run();
        return mfcc.getMFCC();
    }

    public static float cosine(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(na) * Math.sqrt(nb)));
    }
}