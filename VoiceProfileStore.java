// VoiceProfileStore.java
package com.example.voice;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.util.Arrays;

public class VoiceProfileStore {
    private final Context ctx;

    public VoiceProfileStore(Context ctx) { this.ctx = ctx; }

    private EncryptedSharedPreferences prefs() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
        return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                ctx,
                "voice_profiles",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public void saveProfile(String userId, float[] embedding) throws Exception {
        String key = "profile_" + Integer.toHexString(userId.hashCode());
        String serialized = Arrays.toString(embedding); // simple serialization
        prefs().edit().putString(key, serialized).apply();
    }

    public float[] loadProfile(String userId) throws Exception {
        String key = "profile_" + Integer.toHexString(userId.hashCode());
        String s = prefs().getString(key, null);
        if (s == null) return null;
        // Deserialize from "[x, y, z]"
        s = s.replace("[", "").replace("]", "").trim();
        String[] parts = s.split(",");
        float[] out = new float[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = Float.parseFloat(parts[i].trim());
        return out;
    }
}