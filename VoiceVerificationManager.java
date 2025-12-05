// VoiceVerificationManager.java
package com.example.voice;

import android.content.Context;

public class VoiceVerificationManager {
    private final AudioRecorderUtil recorder = new AudioRecorderUtil();
    private final MfccExtractor mfcc = new MfccExtractor(AudioRecorderUtil.SAMPLE_RATE);
    private final VoiceProfileStore store;

    public VoiceVerificationManager(Context ctx) { this.store = new VoiceProfileStore(ctx); }

    public interface VerifyCallback {
        void onResult(boolean verified, float similarity);
        void onError(Exception e);
    }

    public void verify(String userId, float threshold, VerifyCallback cb) {
        try {
            float[] profile = store.loadProfile(userId);
            if (profile == null) { cb.onError(new Exception("Profile not found.")); return; }

            recorder.record(6000, new AudioRecorderUtil.Callback() {
                @Override public void onData(short[] pcm) {
                    float[] f32 = AudioRecorderUtil.toFloat32(pcm);
                    float[] emb = mfcc.compute(f32);
                    float sim = MfccExtractor.cosine(profile, emb);
                    cb.onResult(sim >= threshold, sim);
                }
                @Override public void onError(Exception e) { cb.onError(e); }
            });
        } catch (Exception e) {
            cb.onError(e);
        }
    }
}