package com.example.voice;

import android.content.Context;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.File;   // <-- this import was missing

public class Transcriber {
    private Model model;

    public void init(Context ctx) throws Exception {
        // Ensure model is copied from assets/models/<model>
        File modelDir = AssetUtil.copyAssetFolder(ctx, "models/vosk-model-small-en-us-0.15");
        model = new Model(modelDir.getAbsolutePath());
    }

    public String transcribe(short[] pcm) throws Exception {
        Recognizer rec = new Recognizer(model, AudioRecorderUtil.SAMPLE_RATE);

        // Convert PCM shorts to bytes
        byte[] bytes = new byte[pcm.length * 2];
        for (int i = 0; i < pcm.length; i++) {
            bytes[2 * i] = (byte) (pcm[i] & 0xFF);
            bytes[2 * i + 1] = (byte) ((pcm[i] >> 8) & 0xFF);
        }

        InputStream in = new ByteArrayInputStream(bytes);
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) >= 0) {
            rec.acceptWaveForm(buf, n);
        }

        String resultJson = rec.getFinalResult();
        // Result is JSON: {"text": "recognized text"}
        String text = resultJson.replaceAll(".*\"text\"\\s*:\\s*\"(.*?)\".*", "$1");

        rec.close();
        return text;
    }
}