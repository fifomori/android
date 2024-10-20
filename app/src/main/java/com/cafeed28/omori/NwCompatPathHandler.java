package com.cafeed28.omori;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class NwCompatPathHandler {
    private final String TAG = this.getClass().getSimpleName();

    private final AssetManager mAssets;
    private final String mDirectory;

    private static final List<String> mOneLoaderBlockList = Arrays.asList(
            "js/libs/pixi.js",
            "js/libs/pixi-tilemap.js",
            "js/libs/pixi-picture.js"
    );

    public NwCompatPathHandler(AssetManager assets, String directory) {
        mAssets = assets;
        mDirectory = directory;
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
            return Files.newInputStream(Paths.get(mDirectory, path));
        } catch (IOException e) {
            if (!(e instanceof NoSuchFileException)) {
                Debug.i().log(Log.ERROR, e.toString());
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    private InputStream handleAsset(String path) {
        InputStream is = null;

        if (BuildConfig.DEBUG) {
            try {
                is = Files.newInputStream(Paths.get(mDirectory, "assets", path));
            } catch (IOException e) {
                if (!(e instanceof NoSuchFileException)) {
                    Debug.i().log(Log.ERROR, e.toString());
                    e.printStackTrace();
                }
            }
        }

        if (is == null) {
            try {
                is = mAssets.open(path);
            } catch (IOException e) {
                if (!(e instanceof FileNotFoundException)) {
                    Debug.i().log(Log.ERROR, e.toString());
                    e.printStackTrace();
                }
            }
        }

        return is;
    }

    public WebResourceResponse handle(String path, boolean oneLoader) {
        boolean block = false;
        if (oneLoader) {
            block = mOneLoaderBlockList.contains(path);
            path = path.replace("index.html", "index-oneloader.html");
        }

        InputStream is;
        if (block) {
            is = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        } else {
            is = handleAsset(path);
            if (is == null) is = handleGame(path);
            if (is == null) {
                Debug.i().log(Log.INFO, "%s: file not found: '%s' ('%s')", TAG, path, mDirectory);
                return null;
            }
        }

        return new WebResourceResponse(getMimeType(path), null, is);
    }
}