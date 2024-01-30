package com.cafeed28.omori;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
