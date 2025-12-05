// VoiceEnrollmentManager.java
package com.example.voice;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class VoiceEnrollmentManager {
    private final AudioRecorderUtil recorder = new AudioRecorderUtil();
    private final MfccExtractor mfcc = new MfccExtractor(AudioRecorderUtil.SAMPLE_RATE);
    private final VoiceProfileStore store;

    public VoiceEnrollmentManager(Context ctx) { this.store = new VoiceProfileStore(ctx); }

    public interface EnrollCallback {
        void onComplete();
        void onError(Exception e);
    }

    public void enroll(String userId, int samples, EnrollCallback cb) {
        try {
            List<float[]> embeds = new ArrayList<>();
            recordSample(0, samples, embeds, userId, cb);
        } catch (Exception e) {
            cb.onError(e);
        }
    }

    private void recordSample(int idx, int total, List<float[]> embeds, String userId, EnrollCallback cb) {
        recorder.record(8000, new AudioRecorderUtil.Callback() {
            @Override public void onData(short[] pcm) {
                float[] f32 = AudioRecorderUtil.toFloat32(pcm);
                float[] emb = mfcc.compute(f32);
                embeds.add(emb);
                if (idx + 1 < total) {
                    recordSample(idx + 1, total, embeds, userId, cb);
                } else {
                    // Average embeddings
                    int dims = embeds.get(0).length;
                    float[] avg = new float[dims];
                    for (float[] e : embeds)
                        for (int d = 0; d < dims; d++) avg[d] += e[d];
                    for (int d = 0; d < dims; d++) avg[d] /= embeds.size();
                    try {
                        store.saveProfile(userId, avg);
                        cb.onComplete();
                    } catch (Exception e) {
                        cb.onError(e);
                    }
                }
            }
            @Override public void onError(Exception e) { cb.onError(e); }
        });
    }
}