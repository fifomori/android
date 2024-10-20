package com.cafeed28.omori;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class OmoApplication extends Application {
    private SharedPreferences preferences;

    public SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return preferences;
    }
}
