package com.cafeed28.omori;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class GameActivity extends Activity {
    private OmoWebView mWebView;
    private Dialog mMenuDialog;
    private Dialog mQuitDialog;

    private void hideSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mWebView = findViewById(R.id.webView);
        mWebView.setOnCloseWindowListener(this::finishAndRemoveTask);
        mWebView.start();

        OmoApplication application = (OmoApplication) getApplicationContext();
        SharedPreferences preferences = application.getPreferences();
        float opacityPressed = preferences.getInt(getString(R.string.preference_opacity_pressed), 100) / 100.f;
        float opacityReleased = preferences.getInt(getString(R.string.preference_opacity_released), 25) / 100.f;
        int alphaPressed = (int)(255 * opacityPressed);
        int alphaReleased = (int)(255 * opacityReleased);

        for (var entry : NwCompat.ID_BUTTON_MAPPER.entrySet()) {
            ButtonView button = findViewById(entry.getKey());
            button.setParams(alphaPressed, alphaReleased);
            button.setListener(pressed -> mWebView.dispatchButton(entry.getValue(), pressed));
        }

        final CharSequence[] menuItems = new CharSequence[] {"Toggle FPS counter", "Quit game"};

        mMenuDialog = new AlertDialog.Builder(this)
                .setTitle("Menu")
                .setItems(menuItems, (d, w) -> {
                    switch (w) {
                        case 0: // Toggle FPS counter
                            mWebView.eval("Graphics._toggleFPSCounter();");
                            break;
                        case 1: // Quit game
                            mQuitDialog.show();
                            break;
                    }
                    d.dismiss();
                })
                .create();

        mQuitDialog = new AlertDialog.Builder(this)
                .setTitle("Quit game")
                .setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", (d, w) -> finishAndRemoveTask())
                .setNegativeButton("No", (d, w) -> d.cancel())
                .create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();

        if (mWebView != null) {
            mWebView.resumeTimers();
            mWebView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mWebView != null) {
            mWebView.pauseTimers();
            mWebView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        mMenuDialog.show();
    }
}