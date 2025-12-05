package com.example.voice;

import android.content.Context;
import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;

public class VoiceModel {

    private static Interpreter interpreter;

    public static void loadModel(Context context) throws Exception {
        if (interpreter != null) return;

        ByteBuffer model = AssetUtil.loadModelFile(context, "voice_model.tflite");
        interpreter = new Interpreter(model);
    }

    public static float[] extractEmbedding(float[] audioInput) throws Exception {
        if (interpreter == null)
            throw new Exception("Model not loaded. Call VoiceModel.loadModel(context)");

        float[][] input = new float[1][audioInput.length];
        input[0] = audioInput;

        float[][] output = new float[1][256]; // adjust to your model

        interpreter.run(input, output);  // <-- this compiles once dependency is present;
        return output[0];
    }
}