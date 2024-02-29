package com.cafeed28.omori;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            new Debug(getFilesDir().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fatal error: failed to init logging", Toast.LENGTH_LONG).show();
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        var fragment = new SettingsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_preferences, fragment)
                .commit();

        Button playButton = findViewById(R.id.button_play);

        fragment.setOnPreferencesUpdateListener((preferences) -> {
            playButton.setEnabled(fragment.canPlay(this, preferences));
        });

        playButton.setEnabled(fragment.canPlay(this, PreferenceManager.getDefaultSharedPreferences(this)));
        playButton.setOnClickListener(v -> {
            startActivity(new Intent(this, WebViewActivity.class));
            finish();
        });
    }
}
