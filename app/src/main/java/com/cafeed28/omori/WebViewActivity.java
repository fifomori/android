package com.cafeed28.omori;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends Activity {
    private WebView mWebView;
    private WebViewHelper mWebViewHelper;

    private void hideSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SettingsActivity.initPreferences(this)) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_webview);

        mWebView = findViewById(R.id.webView);
        mWebViewHelper = new WebViewHelper(mWebView, this);

        Map<String, String> noCacheHeaders = new HashMap<>(2);
        noCacheHeaders.put("Pragma", "no-cache");
        noCacheHeaders.put("Cache-Control", "no-cache");

        File file = new File(SettingsActivity.directory + "/index.html");
        mWebView.loadUrl(Uri.fromFile(file).toString(), noCacheHeaders);

        ((ButtonsView) findViewById(R.id.dpad)).setButtonsListener((button, pressed) -> {
            mWebViewHelper.dispatchButton(button + 12, pressed);
        });

        ((ButtonsView) findViewById(R.id.buttons)).setButtonsListener((button, pressed) -> {
            // see nwcompat.gamepad
            int iButton;
            switch (button) {
                case ButtonsView.RIGHT:
                    iButton = 0;
                    break;
                case ButtonsView.BOTTOM:
                    iButton = 1;
                    break;
                case ButtonsView.TOP:
                    iButton = 2;
                    break;
                case ButtonsView.LEFT:
                    iButton = 3;
                    break;
                default:
                    Log.e("Buttons", String.format("Out of range button: %d", button));
                    return;
            }

            mWebViewHelper.dispatchButton(iButton, pressed);
        });
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
        mWebView.evaluateJavascript("TouchInput._onCancel()", null);
    }
}