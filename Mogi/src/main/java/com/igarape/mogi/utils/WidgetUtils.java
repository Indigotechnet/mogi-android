package com.igarape.mogi.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.igarape.mogi.widget.MogiAppWidgetProvider;

public class WidgetUtils {
    public static final String ACTION_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String ACTION_WAITING = "android.appwidget.action.APPWIDGET_WAITING";
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
        update(ctx, ACTION_UPDATE);
    }

    public static void WaitingStateWidget(Context ctx) {
        update(ctx, ACTION_WAITING);
    }

    private static void update(Context ctx, String actionUpdate) {
        context = ctx;
        Intent intent = new Intent(context, MogiAppWidgetProvider.class);
        intent.setAction(actionUpdate);
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), MogiAppWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

}
