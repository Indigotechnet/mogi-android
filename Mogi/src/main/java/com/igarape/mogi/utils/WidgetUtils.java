package com.igarape.mogi.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.igarape.mogi.widget.MogiAppWidgetProvider;

public class WidgetUtils {
    private static boolean isUpdating = false;
    private static Context context;


    public static void BeginUpdating(Context ctx) {
        if (!isUpdating) {
            context = ctx;
            UpdateWidget(ctx);

        }
    }

    public static void StopUpdating() {
        isUpdating = false;
    }

    public static void UpdateWidget(Context ctx) {
        context = ctx;
        Intent intent = new Intent(context, MogiAppWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), MogiAppWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
