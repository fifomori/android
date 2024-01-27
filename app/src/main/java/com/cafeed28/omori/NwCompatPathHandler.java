package com.cafeed28.omori;

import android.app.Activity;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class NwCompatPathHandler {
    private static final Map<String, String> mPathMap = new HashMap<>();

    static {
        mPathMap.put("js/libs/pixi.js", "nwcompat.js");
        mPathMap.put("js/libs/pixi-tilemap.js", "dist/require.js");
        mPathMap.put("js/libs/pixi-picture.js", "dist/patches.js");
    }

    private final String TAG = this.getClass().getSimpleName();
    private final Activity mActivity;

    public NwCompatPathHandler(Activity activity) {
        mActivity = activity;
    }

    private static String getMimeType(@NonNull String path) {
        int lastIndexOf = path.lastIndexOf(".");
        String extension = "";
        if (lastIndexOf != -1) {
            extension = path.substring(lastIndexOf).toLowerCase();
        }

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Nullable
    private InputStream handleGame(String path) {
        try {
            return Files.newInputStream(Paths.get(SettingsActivity.directory, path));
        } catch (IOException e) {
            if (e instanceof NoSuchFileException) {
                Log.d(TAG, String.format("handleGamePath: file not found: '%s'", path));
            }
        }
        return null;
    }

    @Nullable
    private InputStream handleAsset(String path) {
        try {
            if (BuildConfig.DEBUG) {
                return Files.newInputStream(Paths.get(SettingsActivity.directory, "assets", path));
            } else {
                return mActivity.getAssets().open(path);
            }
        } catch (IOException e) {
            if (e instanceof NoSuchFileException) {
                Log.d(TAG, String.format("handleAssetsPath: file not found: '%s'", path));
            }
        }
        return null;
    }

    public WebResourceResponse handle(String path) {
        InputStream is;
        String finalPath = mPathMap.get(path);
        if (finalPath == null) {
            finalPath = path;
            is = handleGame(finalPath);
        } else {
            is = handleAsset(finalPath);
        }

        if (is == null) return null;
        return new WebResourceResponse(getMimeType(finalPath), null, is);
    }
}