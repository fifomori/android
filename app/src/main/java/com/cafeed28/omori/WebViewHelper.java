package com.cafeed28.omori;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WebViewHelper {
    private static final Map<String, String> mInterceptorMap = new HashMap<>();

    static {
        mInterceptorMap.put("js/libs/pixi.js", "nwcompat.js");
        mInterceptorMap.put("js/libs/pixi-tilemap.js", "dist/require.js");
        mInterceptorMap.put("js/libs/pixi-picture.js", "dist/patches.js");
    }

    private static final String TAG = "Console";

    private final WebView mView;
    private final Activity mActivity;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewHelper(WebView view, Activity activity) {
        mView = view;
        mActivity = activity;

        ContextWrapper contextWrapper = new ContextWrapper(activity);
        String dataDir = contextWrapper.getFilesDir().getPath();
        Toast.makeText(activity, dataDir, Toast.LENGTH_LONG).show();

        mView.addJavascriptInterface(new NwCompat(dataDir), NwCompat.INTERFACE);

        mView.setWebViewClient(new ViewClient());
        mView.setWebChromeClient(new ChromeClient());

        mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mView.setKeepScreenOn(true);

        WebSettings settings = mView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
    }

    @SuppressLint("DefaultLocale")
    public void dispatchButton(int button, boolean pressed) {
        String code = String.format("nwcompat.gamepad.buttons[%d].pressed = %b;", button, pressed);
        mView.evaluateJavascript(code, null);
    }

    @SuppressLint("DefaultLocale")
    public void dispatchAxis(int axis, double value) {
        String code = String.format("nwcompat.gamepad.axes[%d] = %f", axis, value);
        Log.d("Kafif", code);
        mView.evaluateJavascript(code, null);
    }

    private InputStream getAsset(String name) {
        try {
            if (BuildConfig.DEBUG) {
                SharedPreferences preferences = mActivity.getSharedPreferences(SettingsActivity.SHARED_PREFERENCES, MODE_PRIVATE);
                String directory = preferences.getString(SettingsActivity.DIRECTORY, null);
                return Files.newInputStream(Paths.get(directory + "/assets/" + name));
            } else {
                return mActivity.getAssets().open(name);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            view.clearCache(true);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            try {
                Path pathAbsolute = Paths.get(request.getUrl().getEncodedPath());
                Path pathBase = Paths.get(SettingsActivity.directory);
                String path = pathBase.relativize(pathAbsolute).toString();

                String interceptPath = mInterceptorMap.get(path);
                if (interceptPath != null) {
                    Log.d("ViewClient", String.format("%s => %s", path, interceptPath));

                    InputStream stream = getAsset(interceptPath);
                    return new WebResourceResponse("text/javascript", StandardCharsets.UTF_8.displayName(), stream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return super.shouldInterceptRequest(view, request);
        }
    }

    private class ChromeClient extends WebChromeClient {
        @Override
        public void onCloseWindow(WebView window) {
            mActivity.finishAndRemoveTask();
            super.onCloseWindow(window);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage message) {
            String basePath;
            try {
                basePath = "file://" + URLDecoder.decode(SettingsActivity.directory, "utf8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            String text = message.message();
            String path = message.sourceId().replace(basePath, "");
            switch (message.messageLevel()) {
                case ERROR: // error
                    text += "\n  from " + path + ":" + message.lineNumber();
                    Log.e(TAG, text);
                    break;
                case WARNING: // warn
                    Log.w(TAG, text);
                    break;
                case LOG: // info, log
                    Log.i(TAG, text);
                    break;
                case TIP: // debug
                case DEBUG: // ?
                    Log.d(TAG, text);
                    break;
            }
            return true;
        }
    }
}
