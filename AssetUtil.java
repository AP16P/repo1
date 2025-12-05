package com.example.voice;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.AssetFileDescriptor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class AssetUtil {

    // Copy an entire folder from assets to external storage (used for Vosk models)
    public static File copyAssetFolder(Context ctx, String assetFolder) throws IOException {
        File outDir = new File(ctx.getExternalFilesDir(null), assetFolder);
        if (!outDir.exists()) outDir.mkdirs();
        copyAll(ctx.getAssets(), assetFolder, outDir);
        return outDir;
    }

    private static void copyAll(AssetManager am, String path, File outDir) throws IOException {
        String[] files = am.list(path);
        if (files == null) return;
        for (String name : files) {
            String assetPath = path + "/" + name;
            String[] children = am.list(assetPath);
            if (children != null && children.length > 0) {
                File sub = new File(outDir, name);
                if (!sub.exists()) sub.mkdirs();
                copyAll(am, assetPath, sub);
            } else {
                File out = new File(outDir, name);
                if (out.exists()) continue;
                InputStream in = am.open(assetPath);
                OutputStream os = new FileOutputStream(out);
                byte[] buf = new byte[4096];
                int read;
                while ((read = in.read(buf)) > 0) os.write(buf, 0, read);
                in.close();
                os.close();
            }
        }
    }

    // Load a single model file (e.g. voice_model.tflite) into ByteBuffer (used for TensorFlow Lite)
    public static ByteBuffer loadModelFile(Context context, String assetName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(assetName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}