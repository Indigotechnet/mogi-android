package com.igarape.mogi.utils;

import android.location.Location;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by felipeamorim on 24/07/2013.
 */
public class FileUtils {

    private static DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    private static String path = "/mnt/extSdCard/smartpolicing/";
    // Environment.getExternalStorageDirectory() + "/external_sd/igarape/"

    public static void setPath(String path) {
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
        LogToFile("battery.txt", level + ";" + status);
    }

    public static void LogLocation(Location location) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);

        LogToFile("locations.txt",
                location.getLatitude() + ";" +
                location.getLongitude() + ";" +
                location.getAccuracy() + ";" +
                location.getProvider() + ";" +
                df.format(new Date())
        );
    }

    private static void LogToFile(String file, String data) {
        data = data + ";" + isoDateFormat.format(new Date());
        try {
            FileWriter writer = new FileWriter(getPath() + file, true);
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            Log.e("FileUtils", e.getMessage());
        }
    }
}
