package com.igarape.mogi.utils;

import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

/**
 * Created by felipeamorim on 24/07/2013.
 */
public class FileUtils {

    public static final String LOCATIONS_TXT = "locations.txt";
    public static final String HISTORY_TXT = "history.txt";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String TAG = FileUtils.class.getName();

    private static String path = "/mnt/extSdCard/smartpolicing/";

    /**
     * @deprecated Implement {@link #getPath(String)} instead.
     */
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

    public static void LogLocation(String userLogin, Location location) {
        try {
            LogToFile(userLogin, LOCATIONS_TXT, LocationUtils.buildJson(location).toString());
        } catch (JSONException e) {
            Log.e(TAG, "error recording location in file", e);

        }
    }
    public static void LogHistory(String userLogin, JSONObject history) {

        LogToFile(userLogin, HISTORY_TXT, history.toString());
    }
    private static void LogToFile(String userLogin, String file, String data) {
        String userPath = getUserPath(userLogin);
        try {
            FileWriter writer = new FileWriter(userPath + file, true);
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * @deprecated Implement {@link #getLocationsFilePath(String)} instead.
     */
    public static String getLocationsFilePath() {
        return getPath() + LOCATIONS_TXT;
    }

    /**
     * @deprecated Implement {@link #getHistoriesFilePath(String)} instead.
     */
    public static String getHistoriesFilePath() {
        return getPath() + HISTORY_TXT;
    }

    public static String getLocationsFilePath(String userLogin) {
        String userPath = getUserPath(userLogin);

        return userPath + LOCATIONS_TXT;
    }

    public static String getHistoriesFilePath(String userLogin) {
        String userPath = getUserPath(userLogin);

        return userPath + HISTORY_TXT;
    }

    private static String getUserPath(String userLogin) {
        String userPath = getPath() + userLogin + File.separator;

        File f = new File(userPath);
        if (!f.exists()) {
            f.mkdirs();
        }

        return userPath;
    }

    public static String getPath(String userLogin) {
        return getUserPath(userLogin);
    }

    public static String[] getUserFolders() {
        return new File(getPath()).list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }
}
