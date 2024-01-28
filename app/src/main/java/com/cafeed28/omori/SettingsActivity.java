package com.cafeed28.omori;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final String SHARED_PREFERENCES = "Preferences";
    public static final String DIRECTORY = "directory";
    public static final String KEY = "key";
    public static final String ONELOADER = "oneloader";

    public static String directory;
    public static String key;
    public static boolean oneloader;

    private static SharedPreferences preferences;

    private boolean isExternalStorageManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

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

    ActivityResultLauncher<Intent> mStartActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!isExternalStorageManager()) {
            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_LONG).show();
        }
    });

    ActivityResultLauncher<String> mRequestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        if (!granted) {
            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_LONG).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (!isExternalStorageManager()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Toast.makeText(this, "Allow storage permission", Toast.LENGTH_LONG).show();
                mStartActivityForResult.launch(
                        new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                );
            } else {
                mRequestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
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
        ((SwitchCompat) findViewById(R.id.oneloaderSwitch)).setOnCheckedChangeListener((v, checked) -> preferences.edit().putBoolean(ONELOADER, checked).apply());
    }

    private void updateDisplay() {
        ((TextView) findViewById(R.id.dirTextView)).setText(preferences.getString(DIRECTORY, "Select OMORI directory"));
        ((SwitchCompat) findViewById(R.id.oneloaderSwitch)).setChecked(preferences.getBoolean(ONELOADER, false));
    }

    public static boolean initPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        directory = preferences.getString(DIRECTORY, null);
        key = preferences.getString(KEY, null);
        oneloader = preferences.getBoolean(ONELOADER, false);

        return (directory != null && !directory.isEmpty()) && (key != null && !key.isEmpty());
    }
}