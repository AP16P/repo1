    package com.example.voice;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.app.Activity;
    import android.content.pm.PackageManager;
    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    public class MainActivity extends Activity {

        private static final int REQ_RECORD_AUDIO = 1001;
        private AudioRecorderUtil recorder;
        private TextView txtStatus;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            recorder = new AudioRecorderUtil();
            txtStatus = findViewById(R.id.txtStatus);
            Button btnRecord = findViewById(R.id.btnRecord);

            btnRecord.setOnClickListener(v -> {
                if (hasAudioPermission()) {
                    startRecording();
                } else {
                    requestAudioPermission();
                }
            });
        }

        private boolean hasAudioPermission() {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        }

        private void requestAudioPermission() {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQ_RECORD_AUDIO);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQ_RECORD_AUDIO) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording();
                } else {
                    txtStatus.setText("Microphone permission denied.");
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private void startRecording() {
            txtStatus.setText("Recording...");
            new Thread(() -> recorder.record(5000, new AudioRecorderUtil.Callback() {
                @Override
                public void onData(short[] pcm) {
                    runOnUiThread(() -> txtStatus.setText("Captured " + pcm.length + " samples"));

                    try {
                        // 1. Transcribe speech
                        Transcriber transcriber = new Transcriber();
                        transcriber.init(MainActivity.this);
                        String text = transcriber.transcribe(pcm);

                        runOnUiThread(() -> txtStatus.setText("Recognized: " + text));

                        // 2. Extract embedding
                        VoiceModel.loadModel(MainActivity.this);
                        // Convert short[] PCM to float[] for VoiceModel
                        float[] audioFloats = new float[pcm.length];
                        for (int i = 0; i < pcm.length; i++) {
                            audioFloats[i] = pcm[i] / 32768f; // normalize to [-1,1]
                        }
                        float[] embedding = VoiceModel.extractEmbedding(audioFloats);

                        runOnUiThread(() -> txtStatus.setText(
                                "Embedding length: " + embedding.length + "\nText: " + text
                        ));

                    } catch (Exception e) {
                        runOnUiThread(() -> txtStatus.setText("Error: " + e.getMessage()));
                    }
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> txtStatus.setText("Error: " + e.getMessage()));
                }
            })).start();
        }
                    }
