package com.igarape.mogi.utils;

import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by felipeamorim on 24/07/2013.
 */
public class FileUtils {

    public static final String LOCATIONS_TXT = "locations.txt";
    public static final String BATTERY_TXT = "battery.txt";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static String path = "/mnt/extSdCard/smartpolicing/";

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

    public static String getPath() {
        return path;
    }

    public static void LogBattery(int level, int status) {
        LogToFile(BATTERY_TXT, level + ";" + status);
    }

    public static void LogLocation(Location location) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        df.setTimeZone(tz);

        LogToFile(LOCATIONS_TXT,
                location.getLatitude() + ";" +
                        location.getLongitude() + ";" +
                        location.getAccuracy() + ";" +
                        location.getProvider() + ";" +
                        df.format(new Date())
        );
    }

    private static void LogToFile(String file, String data) {
        try {
            FileWriter writer = new FileWriter(getPath() + file, true);
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            Log.e("FileUtils", e.getMessage());
        }
    }

    public static String getLocationsFilePath() {
        return getPath() + LOCATIONS_TXT;
    }

}
