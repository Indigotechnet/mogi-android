package com.igarape.mogi.utils;

import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by felipeamorim on 24/07/2013.
 */
public class FileUtils {

    public static final String LOCATIONS_TXT = "locations.txt";
    public static final String HISTORY_TXT = "history.txt";
    public static final String BATTERY_TXT = "battery.txt";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String TAG = FileUtils.class.getName();

    private static String path = "/mnt/extSdCard/smartpolicing/";

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

        FileUtils.path = path;
    }

    public static void LogBattery(int level, int status) {
        LogToFile(BATTERY_TXT, level + ";" + status);
    }

    public static void LogLocation(Location location) {
        try {
            LogToFile(LOCATIONS_TXT, LocationUtils.buildJson(location).toString());
        } catch (JSONException e) {
            Log.e(TAG, "error recording location in file", e);

        }
    }
    public static void LogHistory(JSONObject history) {

        LogToFile(HISTORY_TXT, history.toString());
    }
    private static void LogToFile(String file, String data) {
        try {
            FileWriter writer = new FileWriter(getPath() + file, true);
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static String getLocationsFilePath() {
        return getPath() + LOCATIONS_TXT;
    }

    public static String getHistoriesFilePath() {
        return getPath() + HISTORY_TXT;
    }
}
