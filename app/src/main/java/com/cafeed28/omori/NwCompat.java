package com.cafeed28.omori;

import android.webkit.JavascriptInterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NwCompat {
    public static final String INTERFACE = "nwcompat";

    private final String mDataDirectory;
    private final String mGameDirectory;
    private final String mKey;

    public NwCompat(String dataDirectory, String gameDirectory, String key) {
        mDataDirectory = dataDirectory;
        mGameDirectory = gameDirectory;
        mKey = key;
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
            return Base64.encode(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            if (!(e instanceof NoSuchFileException)) {
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
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void fsUnlink(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void fsRename(String path, String newPath) {
        try {
            Files.move(Paths.get(path), Paths.get(newPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
