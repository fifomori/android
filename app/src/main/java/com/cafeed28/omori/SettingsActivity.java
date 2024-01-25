package com.cafeed28.omori;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String SHARED_PREFERENCES = "Preferences";
    public static final String DIRECTORY = "directory";
    public static final String KEY = "key";

    public static String directory;
    public static String key;

    private static SharedPreferences preferences;

    ActivityResultLauncher<Uri> mOpenDocumentTree = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
        if (uri != null) {
            String[] pathSections = uri.getPath().split(":");
            String path = Environment.getExternalStorageDirectory().getPath() + "/" + pathSections[pathSections.length - 1];

            // call this to persist permission across device reboots
            // getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            preferences.edit().putString(DIRECTORY, path).apply();
            updateDisplay();
        }
    });

    ActivityResultLauncher<Intent> mRequestStoragePermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!Environment.isExternalStorageManager()) {
            Toast.makeText(SettingsActivity.this, "Storage permission is required", Toast.LENGTH_LONG).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (!Environment.isExternalStorageManager()) {
            Toast.makeText(SettingsActivity.this, "Allow storage permission", Toast.LENGTH_LONG).show();
            mRequestStoragePermission.launch(
                    new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID))
            );
        }

        preferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        updateDisplay();

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("Enter decryption key")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> preferences.edit().putString(KEY, input.getText().toString()).apply())
                .setNegativeButton("Cancel", (d, w) -> d.cancel())
                .create();

        findViewById(R.id.dirButton).setOnClickListener(view -> mOpenDocumentTree.launch(null));
        findViewById(R.id.keyButton).setOnClickListener(view -> dialog.show());
    }

    private void updateDisplay() {
        ((TextView) findViewById(R.id.dirTextView)).setText(preferences.getString(DIRECTORY, "Select OMORI directory"));
    }

    public static boolean initPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        directory = preferences.getString(DIRECTORY, null);
        key = preferences.getString(KEY, null);
        return (directory != null && !directory.isEmpty()) && (key != null && !key.isEmpty());
    }
}