package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class OmoWebView extends WebView {
    public interface OnCloseWindowListener {
        void onCloseWindow();
    }

    private OnCloseWindowListener mOnCloseWindowListener;

    private final AssetManager mAssets;

    private final String mDataDirectory;
    private final String mGameDirectory;
    private final String mKey;
    private final boolean mOneLoader;

    public void setOnCloseWindowListener(OnCloseWindowListener l) {
        mOnCloseWindowListener = l;
    }

    public OmoWebView(Context context) {
        this(context, null, 0, 0);
    }

    public OmoWebView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public OmoWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    private void setFrameRate(Window window, Display display, int frameRate) {
        if (Math.floor(display.getRefreshRate()) == frameRate) {
            Debug.i().log(Log.INFO, "setFrameRate: already set");
            return;
        }

        Display.Mode currentMode = display.getMode();
        Debug.i().log(Log.INFO, "current mode: %s", currentMode);

        Display.Mode targetMode = null;
        for (Display.Mode mode : display.getSupportedModes()) {
            Debug.i().log(Log.INFO, "mode: %s", mode.toString());

            boolean sameResolutionMode = currentMode.getPhysicalWidth() == mode.getPhysicalWidth()
                    && currentMode.getPhysicalHeight() == mode.getPhysicalHeight();
            if (sameResolutionMode && Math.floor(mode.getRefreshRate()) == frameRate) {
                if (targetMode != null) { // should never happen probably
                    Debug.i().log(Log.WARN, "targetMode != null, but found another suitable mode");
                    Debug.i().log(Log.WARN, "targetMode: %s", targetMode.toString());
                    Debug.i().log(Log.WARN, "mode: %s", mode.toString());
                    Toast.makeText(getContext(), "Found multiple suitable display modes, view logs for details", Toast.LENGTH_LONG).show();
                }
                targetMode = mode;
            }
        }

        if (targetMode != null) {
            Debug.i().log(Log.INFO, "targetMode(%d): %s", targetMode.getModeId(), targetMode);
            var attributes = window.getAttributes();
            attributes.preferredRefreshRate = targetMode.getRefreshRate();
            window.setAttributes(attributes);
        } else {
            Debug.i().log(Log.WARN, "targetMode == null");
            Toast.makeText(getContext(), "Suitable display mode not found, view logs for details", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public OmoWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {}

            @Override
            public void onDisplayRemoved(int displayId) {}

            @Override
            public void onDisplayChanged(int displayId) {
                Debug.i().log(Log.INFO, "onDisplayChanged");
                Display display = displayManager.getDisplay(displayId);

                Activity activity = (Activity) getContext();
                Window window = activity.getWindow();

                setFrameRate(window, display, 60);
            }
        }, null);

        OmoApplication application = (OmoApplication) context.getApplicationContext();
        SharedPreferences preferences = application.getPreferences();

        mAssets = context.getAssets();
        mDataDirectory = context.getFilesDir().getPath();
        mGameDirectory = preferences.getString(context.getString(R.string.preference_directory), null);
        mKey = preferences.getString(context.getString(R.string.preference_key), null);
        mOneLoader = preferences.getBoolean(context.getString(R.string.preference_oneloader), false);

        addJavascriptInterface(new NwCompat(this, mDataDirectory, mGameDirectory, mKey), NwCompat.INTERFACE);

        setWebViewClient(new ViewClient());
        setWebChromeClient(new ChromeClient());

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setKeepScreenOn(true);

        WebSettings settings = getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadsImagesAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Activity activity = (Activity) getContext();
        Window window = activity.getWindow();

        setFrameRate(window, getDisplay(), 60);
    }

    public void start() {
        var url = new Uri.Builder()
                .scheme("http")
                .authority("game")
                .appendPath("index.html")
                .build()
                .toString();

        loadUrl(url);
    }

    public void dispatchButton(int button, boolean pressed) {
        eval("nwcompat.gamepad.buttons[%d].pressed = %b;", button, pressed);
    }

    public void dispatchAxis(int axis, double value) {
        eval("nwcompat.gamepad.axes[%d] = %f", axis, value);
    }

    public void eval(String format, Object... args) {
        String code = String.format(Locale.ENGLISH, format, args);
        evaluateJavascript(code, null);
    }

    private class ViewClient extends WebViewClient {
        private final NwCompatPathHandler mPathHandler = new NwCompatPathHandler(mAssets, mGameDirectory);

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            var path = request.getUrl().getEncodedPath();
            if (path == null) return null;
            if (path.charAt(0) == '/') path = path.substring(1);

            if (path.contains("%")) {
                var decodedPath = Uri.decode(path);
                if (!decodedPath.contains("\0")) path = decodedPath;
            }

            return mPathHandler.handle(path, mOneLoader);
        }
    }

    private class ChromeClient extends WebChromeClient {
        @Override
        public void onCloseWindow(WebView window) {
            if (mOnCloseWindowListener != null) {
                mOnCloseWindowListener.onCloseWindow();
            }
            super.onCloseWindow(window);
        }

        @Override
        public boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage) {
            var logLine = String.format("[CONSOLE]: %s", consoleMessage.message());

            if (BuildConfig.DEBUG) {
                switch (consoleMessage.messageLevel()) {
                    case ERROR: // error
                        Debug.i().log(Log.ERROR, "%s\n  from %s:%s", logLine, consoleMessage.sourceId(), consoleMessage.lineNumber());
                        break;
                    case WARNING: // warn
                        Debug.i().log(Log.WARN, logLine);
                        break;
                    case LOG: // info, log
                        Debug.i().log(Log.INFO, logLine);
                        break;
                    case TIP: // debug
                    case DEBUG: // ?
                        Debug.i().log(Log.DEBUG, logLine);
                        break;
                }
            }

            return true;
        }
    }
}
