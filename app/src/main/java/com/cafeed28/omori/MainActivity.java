package com.cafeed28.omori;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

        Button playButton = findViewById(R.id.button_play);

        ViewCompat.setOnApplyWindowInsetsListener(playButton, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.bottomMargin = insets.bottom;
            v.setLayoutParams(mlp);

            return WindowInsetsCompat.CONSUMED;
        });

        var fragment = new SettingsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_preferences, fragment)
                .commit();

        fragment.setOnPreferencesUpdateListener((preferences) -> {
            playButton.setEnabled(fragment.canPlay(this, preferences));
        });

        View framePreferences = findViewById(R.id.frame_preferences);

        ViewCompat.setOnApplyWindowInsetsListener(framePreferences, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            v.setLayoutParams(mlp);

            return WindowInsetsCompat.CONSUMED;
        });

        playButton.setEnabled(fragment.canPlay(this, PreferenceManager.getDefaultSharedPreferences(this)));
        playButton.setOnClickListener(v -> {
            startActivity(new Intent(this, GameActivity.class));
            finish();
        });
    }
}
