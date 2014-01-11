package com.igarape.mogi.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.igarape.mogi.widget.MogiAppWidgetProvider;

/**
 * Created by felipeamorim on 26/08/2013.
 */
public class ServerUtils {

    // ec2-54-234-158-63.compute-1.amazonaws.com
    private final static String serverUrl = "http://54.221.244.181:3000";

    public static String getServerUrl(String path) {
        return String.format("%s%s", serverUrl, path);
    }

}

