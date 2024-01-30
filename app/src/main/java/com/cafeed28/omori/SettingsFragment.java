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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SettingsFragment extends PreferenceFragmentCompat {
    public interface OnPreferencesUpdateListener {
        void onPreferencesUpdate(SharedPreferences preferences);
    }

    private OnPreferencesUpdateListener mListener;

    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener = (preferences, key) -> {
        updatePreferences(preferences);
    };

    public static String PREFERENCE_DIRECTORY;
    public static String PREFERENCE_KEY;
    public static String PREFERENCE_ONELOADER;

    private SharedPreferences mPreferences;
    private ActivityResultLauncher<Uri> mOpenDocumentTree;
    private ActivityResultLauncher<String> mRequestPermission;

    private Dialog mOneLoaderDialog;

    public void setOnPreferencesUpdateListener(OnPreferencesUpdateListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        PREFERENCE_DIRECTORY = getString(R.string.preference_directory);
        PREFERENCE_KEY = getString(R.string.preference_key);
        PREFERENCE_ONELOADER = getString(R.string.preference_oneloader);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(prefListener);

        mOpenDocumentTree = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
            if (uri == null) return;

            var uriPath = uri.getPath();
            if (uriPath == null) return;

            String[] pathSections = uriPath.split(":");
            String directory = Environment.getExternalStorageDirectory().getPath() + "/" + pathSections[pathSections.length - 1];
            mPreferences.edit().putString(PREFERENCE_DIRECTORY, directory).apply();

            updatePreferences(mPreferences);
        });

        mRequestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (!granted) {
                Toast.makeText(getContext(), "Storage permission is required", Toast.LENGTH_LONG).show();
            }
        });

        mOneLoaderDialog = new AlertDialog.Builder(getContext())
                .setTitle("OneLoader is not installed")
                .setMessage("To use OneLoader, you must install it first")
                .setPositiveButton("Install", (d, w) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mods.one/mod/oneloader"))))
                .setNegativeButton("Cancel", (d, w) -> d.cancel())
                .create();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference directoryPreference = findPreference(PREFERENCE_DIRECTORY);
        Preference keyPreference = findPreference(PREFERENCE_KEY);
        Preference oneLoaderPreference = findPreference(PREFERENCE_ONELOADER);
        if (directoryPreference == null || keyPreference == null || oneLoaderPreference == null) return;

        directoryPreference.setOnPreferenceClickListener(preference -> {
            if (checkPermissions(getContext())) mOpenDocumentTree.launch(null);
            else requestPermissions();
            return true;
        });

        oneLoaderPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!isOneLoaderInstalled()) {
                mOneLoaderDialog.show();
                return false;
            }
            return true;
        });

        updatePreferences(mPreferences);
    }

    private void updatePreferences(SharedPreferences preferences) {
        if (mListener != null) mListener.onPreferencesUpdate(preferences);

        Preference directoryPreference = findPreference(PREFERENCE_DIRECTORY);
        Preference oneLoaderPreference = findPreference(PREFERENCE_ONELOADER);
        if (directoryPreference == null || oneLoaderPreference == null) return;

        directoryPreference.setSummary(String.format("Current: %s", preferences.getString(PREFERENCE_DIRECTORY, "not set")));
        oneLoaderPreference.setEnabled(canPlay(getContext(), mPreferences));
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(getContext(), "Allow all files access", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
        } else {
            mRequestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private boolean checkPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public boolean canPlay(Context context, SharedPreferences preferences) {
        String directory = preferences.getString(SettingsFragment.PREFERENCE_DIRECTORY, null);
        String key = preferences.getString(SettingsFragment.PREFERENCE_KEY, null);

        return directory != null && !directory.isEmpty() &&
                key != null && !key.isEmpty() &&
                checkPermissions(context);
    }

    private boolean isOneLoaderInstalled() {
        var directory = mPreferences.getString(PREFERENCE_DIRECTORY, null);
        return Files.exists(Paths.get(directory, "modloader", "early_loader.js"));
    }
}