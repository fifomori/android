package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContextWrapper;
import android.net.Uri;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

public class WebViewHelper {
    private final WebView mView;
    private final Activity mActivity;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewHelper(WebView view, Activity activity) {
        mView = view;
        mActivity = activity;

        ContextWrapper contextWrapper = new ContextWrapper(activity);
        String dataDir = contextWrapper.getFilesDir().getPath();
        mView.addJavascriptInterface(new NwCompat(dataDir), NwCompat.INTERFACE);

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
        private final NwCompatPathHandler mPathHandler = new NwCompatPathHandler(mActivity);

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

                return mPathHandler.handle(path);
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
    }
}
