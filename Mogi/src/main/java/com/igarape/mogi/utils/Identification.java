package com.igarape.mogi.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Created by felipeamorim on 26/08/2013.
 */
public class Identification {
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static final String PREF_ACCESS_TOKEN = "PREF_ACCESS_TOKEN";
    private static final String PREF_USER_LOGIN = "PREF_USER_LOGIN";
    private static final String PREF_TIME_LOGIN = "PREF_TIME_LOGIN";
    private static String uniqueID = null;
    private static String accessToken = null;
    private static String userLogin = null;
    private static String serverIpAddress = "";
    private static Integer streamingPort = 1935;
    private static String streamingUser = "";
    private static String streamingPassword = "";
    private static String streamingPath = "";

    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return uniqueID;
    }

    public synchronized static String getAccessToken(Context context) {
        if (accessToken == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
            accessToken = sharedPrefs.getString(PREF_ACCESS_TOKEN, null);
        }
        return accessToken != null ? "Bearer " + accessToken : null;
    }

    public synchronized static void setAccessToken(Context context, String token) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_ACCESS_TOKEN, token);
        editor.putLong(PREF_TIME_LOGIN, java.lang.System.currentTimeMillis());
        editor.commit();
        accessToken = token;
    }

    public synchronized static long getTimeLogin(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        return sharedPrefs.getLong(PREF_TIME_LOGIN, -1);
    }

    public synchronized static String getUserLogin(Context context) {
        if (userLogin == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
            userLogin = sharedPrefs.getString(PREF_USER_LOGIN, null);
        }
        return userLogin;
    }

    public synchronized static void setUserLogin(Context context, String login) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_USER_LOGIN, login);
        editor.commit();
    }

    public static String getServerIpAddress() {
        return serverIpAddress;
    }

    public static void setServerIpAddress(String serverIpAddress) {
        Identification.serverIpAddress = serverIpAddress;
    }

    public static Integer getStreamingPort() {
        return streamingPort;
    }

    public static void setStreamingPort(Integer streamingPort) {
        Identification.streamingPort = streamingPort;
    }

    public static String getStreamingUser() {
        return streamingUser;
    }

    public static void setStreamingUser(String streamingUser) {
        Identification.streamingUser = streamingUser;
    }

    public static String getStreamingPassword() {
        return streamingPassword;
    }

    public static void setStreamingPassword(String streamingPassword) {
        Identification.streamingPassword = streamingPassword;
    }

    public static String getStreamingPath() {
        return streamingPath;
    }

    public static void setStreamingPath(String streamingPath) {
        Identification.streamingPath = streamingPath;
    }
}
