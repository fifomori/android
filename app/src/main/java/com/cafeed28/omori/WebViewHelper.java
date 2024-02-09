package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class WebViewHelper {
    private final WebView mView;
    private final Activity mActivity;

    private final String mDataDirectory;
    private final String mGameDirectory;
    private final String mKey;
    private final boolean mOneLoader;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewHelper(WebView view, Activity activity) {
        mView = view;
        mActivity = activity;

        ContextWrapper contextWrapper = new ContextWrapper(activity);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        mDataDirectory = contextWrapper.getFilesDir().getPath();
        mGameDirectory = preferences.getString(activity.getString(R.string.preference_directory), null);
        mKey = preferences.getString(activity.getString(R.string.preference_key), null);
        mOneLoader = preferences.getBoolean(activity.getString(R.string.preference_oneloader), false);

        mView.addJavascriptInterface(new NwCompat(mView, mDataDirectory, mGameDirectory, mKey), NwCompat.INTERFACE);

        mView.setWebViewClient(new ViewClient());
        mView.setWebChromeClient(new ChromeClient());

        mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mView.setKeepScreenOn(true);

        WebSettings settings = mView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadsImagesAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
    }

    public void start() {
        var url = new Uri.Builder()
                .scheme("http")
                .authority("game")
                .appendPath("index.html")
                .build()
                .toString();

        Map<String, String> noCacheHeaders = new HashMap<>(2);
        noCacheHeaders.put("Pragma", "no-cache");
        noCacheHeaders.put("Cache-Control", "no-cache");

        mView.loadUrl(url, noCacheHeaders);
    }

    @SuppressLint("DefaultLocale")
    public void dispatchButton(int button, boolean pressed) {
        String code = String.format("nwcompat.gamepad.buttons[%d].pressed = %b;", button, pressed);
        mView.evaluateJavascript(code, null);
    }

    @SuppressLint("DefaultLocale")
    public void dispatchAxis(int axis, double value) {
        String code = String.format("nwcompat.gamepad.axes[%d] = %f", axis, value);
        mView.evaluateJavascript(code, null);
    }

    private class ViewClient extends WebViewClient {
        private final NwCompatPathHandler mPathHandler = new NwCompatPathHandler(mActivity, mGameDirectory);

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            try {
                var path = request.getUrl().getEncodedPath();
                if (path == null) return null;
                if (path.charAt(0) == '/') path = path.substring(1);

                if (path.contains("%")) {
                    var decodedPath = Uri.decode(path);
                    if (!decodedPath.contains("\0")) path = decodedPath;
                }

                return mPathHandler.handle(path, mOneLoader);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class ChromeClient extends WebChromeClient {
        @Override
        public void onCloseWindow(WebView window) {
            mActivity.finishAndRemoveTask();
            super.onCloseWindow(window);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            var message = consoleMessage.message();

            if (BuildConfig.DEBUG) {
                switch (consoleMessage.messageLevel()) {
                    case ERROR: // error
                        Log.e("Console", message + "\n  from " + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                        break;
                    case WARNING: // warn
                        Log.w("Console", message);
                        break;
                    case LOG: // info, log
                        Log.i("Console", message);
                        break;
                    case TIP: // debug
                    case DEBUG: // ?
                        Log.d("Console", message);
                        break;
                }
            }

            return true;
        }
    }
}
