package com.cafeed28.omori;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;
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
    public static String PREFERENCE_LOGS;
    public static String PREFERENCE_LOGS_CLEAR;

    private SharedPreferences mPreferences;
    private ActivityResultLauncher<Uri> mOpenDocumentTree;
    private ActivityResultLauncher<String> mRequestPermission;

    private Dialog mOneLoaderDialog;
    private Activity mActivity;

    public void setOnPreferencesUpdateListener(OnPreferencesUpdateListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();

        PREFERENCE_DIRECTORY = getString(R.string.preference_directory);
        PREFERENCE_KEY = getString(R.string.preference_key);
        PREFERENCE_ONELOADER = getString(R.string.preference_oneloader);
        PREFERENCE_LOGS = getString(R.string.preference_logs);
        PREFERENCE_LOGS_CLEAR = getString(R.string.preference_logs_clear);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(prefListener);

        mOpenDocumentTree = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
            if (uri == null) return;

            var uriPath = uri.getPath();
            if (uriPath == null) return;

            String[] pathSections = uriPath.split(":");
            Debug.i().log(Log.INFO, "selected directory: %s", uriPath);
            String directory = Environment.getExternalStorageDirectory().getPath() + "/" + pathSections[pathSections.length - 1];

            if (!Files.exists(Paths.get(directory, "index.html"))) {
                Toast.makeText(context, "Selected directory is not a game directory (index.html not found)", Toast.LENGTH_LONG).show();
                return;
            }

            mPreferences.edit().putString(PREFERENCE_DIRECTORY, directory).apply();
            updatePreferences(mPreferences);
        });

        mRequestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (!granted) {
                Toast.makeText(mActivity, "Storage permission is required", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(mActivity, "Restart app now", Toast.LENGTH_LONG).show();
            mActivity.finishAndRemoveTask();
        });

        mOneLoaderDialog = new AlertDialog.Builder(context)
                .setTitle("OneLoader is not installed")
                .setMessage("To use OneLoader, you must install it first")
                .setPositiveButton("Install", (d, w) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mods.one/mod/oneloader"))))
                .setNegativeButton("Cancel", (d, w) -> d.cancel())
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences(mPreferences);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // @todo: is there a way to do this better?
        Preference directoryPreference = findPreference(PREFERENCE_DIRECTORY);
        Preference oneLoaderPreference = findPreference(PREFERENCE_ONELOADER);
        Preference logsPreference = findPreference(PREFERENCE_LOGS);
        Preference logsClearPreference = findPreference(PREFERENCE_LOGS_CLEAR);
        if (directoryPreference == null || oneLoaderPreference == null || logsPreference == null || logsClearPreference == null)
            return;

        directoryPreference.setOnPreferenceClickListener(preference -> {
            if (checkPermissions(mActivity)) mOpenDocumentTree.launch(null);
            else requestPermissions();
            return true;
        });

        logsPreference.setOnPreferenceClickListener(preference -> {
            Debug.i().save(mActivity);
            return true;
        });

        logsClearPreference.setOnPreferenceClickListener(preference -> {
            Debug.i().clear(mActivity, true);
            Toast.makeText(mActivity, "Restart app now", Toast.LENGTH_LONG).show();
            mActivity.finishAndRemoveTask();
            return true;
        });

        oneLoaderPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) { // allow user to turn off even if oneloader isn't installed
                if (!isOneLoaderInstalled()) {
                    mOneLoaderDialog.show();
                    return false;
                }
            }

            return true;
        });

        updatePreferences(mPreferences);
    }

    private void updatePreferences(SharedPreferences preferences) {
        if (mListener != null) mListener.onPreferencesUpdate(preferences);

        Preference directoryPreference = findPreference(PREFERENCE_DIRECTORY);
        Preference oneLoaderPreference = findPreference(PREFERENCE_ONELOADER);
        Preference logsPreference = findPreference(PREFERENCE_LOGS);
        Preference logsClearPreference = findPreference(PREFERENCE_LOGS_CLEAR);
        if (directoryPreference == null || oneLoaderPreference == null || logsPreference == null || logsClearPreference == null)
            return;

        directoryPreference.setSummary(String.format("Current: %s", preferences.getString(PREFERENCE_DIRECTORY, "not set")));
        oneLoaderPreference.setEnabled(canPlay(mActivity, mPreferences));

        boolean filesPermission = checkPermissions(mActivity);
        if (!logsPreference.isEnabled() && filesPermission) {
            Toast.makeText(mActivity, "Restart app now", Toast.LENGTH_LONG).show();
            mActivity.finishAndRemoveTask();
        }

        logsPreference.setEnabled(filesPermission);
        logsClearPreference.setEnabled(filesPermission);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(mActivity, "Allow all files access", Toast.LENGTH_LONG).show();
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
