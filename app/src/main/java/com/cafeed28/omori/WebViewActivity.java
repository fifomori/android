package com.cafeed28.omori;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Map;

public class WebViewActivity extends Activity {
    private WebView mWebView;
    private WebViewHelper mWebViewHelper;
    private boolean mBackClickedOnce;

    // see nwcompat.gamepad
    private final Map<Integer, Integer> mButtonMapper = Map.of(
            R.id.button_a, 0,
            R.id.button_b, 1,
            R.id.button_x, 2,
            R.id.button_y, 3,
            R.id.button_trigger_left, 4,
            R.id.button_trigger_right, 5,
            R.id.button_dpad_up, 12,
            R.id.button_dpad_down, 13,
            R.id.button_dpad_left, 14,
            R.id.button_dpad_right, 15
    );

    private void hideSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = findViewById(R.id.webView);
        mWebViewHelper = new WebViewHelper(mWebView, this);
        mWebViewHelper.start();

        for (var entry : mButtonMapper.entrySet()) {
            ((ButtonView) findViewById(entry.getKey())).setListener(pressed -> {
                mWebViewHelper.dispatchButton(entry.getValue(), pressed);
            });
        }
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
        if (mBackClickedOnce) {
            super.onBackPressed();
            return;
        }

        mBackClickedOnce = true;
        Toast.makeText(this, "Click back twice to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mBackClickedOnce = false;
        }, 2000);
    }
}