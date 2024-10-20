package com.cafeed28.omori;

import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class NwCompat {
    public static final String INTERFACE = "nwcompat";

    // see nwcompat.gamepad
    public static final Map<Integer, Integer> ID_BUTTON_MAPPER = Map.of(
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

    public static final Map<Integer, Integer> KEY_BUTTON_MAPPER = Map.of(
            KeyEvent.KEYCODE_BUTTON_A, 0,
            KeyEvent.KEYCODE_BUTTON_B, 1,
            KeyEvent.KEYCODE_BUTTON_X, 2,
            KeyEvent.KEYCODE_BUTTON_Y, 3,
            KeyEvent.KEYCODE_BUTTON_L1, 4,
            KeyEvent.KEYCODE_BUTTON_R1, 5,
            KeyEvent.KEYCODE_DPAD_UP, 12,
            KeyEvent.KEYCODE_DPAD_DOWN, 13,
            KeyEvent.KEYCODE_DPAD_LEFT, 14,
            KeyEvent.KEYCODE_DPAD_RIGHT, 15
    );

    private final WebView mView;
    private final String mDataDirectory;
    private final String mGameDirectory;
    private final String mKey;

    private final Base64.Decoder mDecoder = Base64.getDecoder();
    private final Base64.Encoder mEncoder = Base64.getEncoder();

    public NwCompat(WebView view, String dataDirectory, String gameDirectory, String key) {
        mView = view;
        mDataDirectory = dataDirectory;
        mGameDirectory = gameDirectory;
        mKey = key;
    }

    @JavascriptInterface
    public void asyncCall(int id, String methodName, String args) {
        NwCompat self = this;

        new Thread(() -> {
            try {
                JSONObject params = new JSONObject(args);
                String result = (String) self.getClass().getMethod(methodName, JSONObject.class).invoke(self, params);
                self.jsResolve(id, true, result);
            } catch (InvocationTargetException ite) {
                self.jsResolve(id, false, ite.getCause().toString());
            } catch (Exception e) {
                self.jsResolve(id, false, e.toString());
            }
        }).start();
    }

    private void jsResolve(int id, boolean success, String result) {
        var formattedResult = result == null ? "null" : String.format("\"%s\"", result);
        var code = String.format("nwcompat.async.callback(%d, %b, %s)", id, success, formattedResult);
        Debug.i().log(Log.DEBUG, code);
        mView.post(() -> mView.evaluateJavascript(code, null));
    }

    public String fsReadFileAsync(JSONObject args) throws JSONException {
        return fsReadFile(args.getString("path"));
    }

    @JavascriptInterface
    public String getDataDirectory() {
        return mDataDirectory;
    }

    @JavascriptInterface
    public String getGameDirectory() {
        return mGameDirectory;
    }

    @JavascriptInterface
    public String getKey() {
        return mKey;
    }

    /**
     * @return -1: error or file not found; 1: file; 2: directory
     */
    @JavascriptInterface
    public int fsStat(String path) {
        File f = new File(path);
        if (f.isFile()) return 1;
        else if (f.isDirectory()) return 2;
        return -1;
    }

    @JavascriptInterface
    public String fsReadDir(String path) {
        List<String> list = new ArrayList<>();

        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                list.add(file.getName());
            }
        }

        return String.join(":", list);
    }

    @JavascriptInterface
    public void fsMkDir(String path) {
        File f = new File(path);
        f.mkdir();
    }

    @JavascriptInterface
    public String fsReadFile(String path) {
        try {
            var bytes = Files.readAllBytes(Paths.get(path));
            return mEncoder.encodeToString(bytes);
        } catch (IOException e) {
            if (!(e instanceof NoSuchFileException)) {
                Debug.i().log(Log.ERROR, e.toString());
                e.printStackTrace();
            }
            return null;
        }
    }

    @JavascriptInterface
    public void fsWriteFile(String path, byte[] data) {
        try {
            Files.write(Paths.get(path), data);
        } catch (IOException e) {
            Debug.i().log(Log.ERROR, e.toString());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void fsUnlink(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            Debug.i().log(Log.ERROR, e.toString());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void fsRename(String path, String newPath) {
        try {
            Files.move(Paths.get(path), Paths.get(newPath));
        } catch (IOException e) {
            Debug.i().log(Log.ERROR, e.toString());
            e.printStackTrace();
        }
    }
}
