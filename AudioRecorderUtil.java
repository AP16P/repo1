package com.example.voice;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorderUtil {

    // Constants for audio recording
    public static final int SAMPLE_RATE = 16000;
    public static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    // Callback interface to deliver results
    public interface Callback {
        void onData(short[] pcm);
        void onError(Exception e);
    }

    // Record audio for maxMillis milliseconds
    public void record(int maxMillis, Callback cb) {
        int minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);

        AudioRecord recorder = null;
        try {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE, CHANNEL, ENCODING, Math.max(minBuf, SAMPLE_RATE));

            recorder.startRecording();  // may throw SecurityException if permission missing

            int totalSamples = (SAMPLE_RATE * maxMillis) / 1000;
            short[] buffer = new short[1024];
            short[] out = new short[totalSamples];
            int idx = 0;

            while (idx < totalSamples) {
                int read = recorder.read(buffer, 0, buffer.length);
                if (read <= 0) break;

                double rms = 0;
                for (int i = 0; i < read; i++) rms += buffer[i] * buffer[i];
                rms = Math.sqrt(rms / read);
                if (rms > 500) { // simple silence gate
                    int copy = Math.min(read, totalSamples - idx);
                    System.arraycopy(buffer, 0, out, idx, copy);
                    idx += copy;
                }
            }
            cb.onData(out);

        } catch (SecurityException se) {
            cb.onError(new Exception("Microphone permission not granted"));
        } catch (Exception e) {
            cb.onError(e);
        } finally {
            if (recorder != null) {
                try { recorder.stop(); } catch (Exception ignore) {}
                recorder.release();
            }
        }
    }

    // Utility to convert PCM16 to float32
    public static float[] toFloat32(short[] pcm) {
        float[] f = new float[pcm.length];
        for (int i = 0; i < pcm.length; i++) f[i] = pcm[i] / 32768f;
        return f;
    }
}