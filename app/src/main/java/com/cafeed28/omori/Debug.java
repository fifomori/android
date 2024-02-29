package com.cafeed28.omori;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Date;

public class Debug {
    private final String TAG = "omori";

    private final DateFormat mDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private final String mInternalFileName;
    private final String mExternalFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/omori.log";
    private final PrintWriter mPrintWriter;

    private static Debug mInstance = null;

    public static Debug i() {
        return mInstance;
    }

    public Debug(String filesPath) throws IOException {
        mInternalFileName = filesPath + "/debug.log";
        var fileWriter = new FileWriter(mInternalFileName, true);
        var bufferedWriter = new BufferedWriter(fileWriter);
        mPrintWriter = new PrintWriter(bufferedWriter);
        mPrintWriter.flush();
        mInstance = this;
    }

    public void log(int level, String format, Object... args) {
        String logLine;
        try {
            if (args.length > 0) logLine = String.format(format, args);
            else logLine = format;
        } catch (IllegalArgumentException e) {
            logLine = format;
        }

        final String levelName;
        switch (level) {
            case Log.VERBOSE:
                Log.v(TAG, logLine);
                levelName = "VERBOSE";
                break;
            case Log.DEBUG:
                Log.d(TAG, logLine);
                levelName = "DEBUG";
                break;
            case Log.INFO:
                Log.i(TAG, logLine);
                levelName = "INFO";
                break;
            case Log.WARN:
                Log.w(TAG, logLine);
                levelName = "WARN";
                break;
            case Log.ERROR:
                Log.e(TAG, logLine);
                levelName = "ERROR";
                break;
            default:
                throw new IllegalArgumentException();
        }

        String dateLine = String.format("[%s] %s: %s", mDateFormat.format(new Date()), levelName, logLine);
        mPrintWriter.println(dateLine);
        mPrintWriter.flush();
    }

    public void clear(Context context, boolean internal) {
        try {
            Files.deleteIfExists(Paths.get(internal ? mInternalFileName : mExternalFileName));
        } catch (IOException e) {
            Toast.makeText(context, "Failed to clear logs, check 'adb logcat' for more info", Toast.LENGTH_LONG).show();
            this.log(Log.ERROR, "Failed to clear logs from '%s'", mInternalFileName);
            e.printStackTrace();
        }
    }

    public void save(Context context) {
        try {
            clear(context, false);
            Files.copy(Paths.get(mInternalFileName), Paths.get(mExternalFileName));
            Toast.makeText(context, String.format("Logs saved in %s", mExternalFileName), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to save logs, check 'adb logcat' for more info", Toast.LENGTH_LONG).show();
            this.log(Log.ERROR, "Failed to save logs to '%s'", mExternalFileName);
            e.printStackTrace();
        }
    }
}
