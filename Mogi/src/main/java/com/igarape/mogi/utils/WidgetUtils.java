package com.igarape.mogi.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.igarape.mogi.widget.MogiAppWidgetProvider;

public class WidgetUtils {
    private static boolean isUpdating = false;
    private static String TAG = WidgetUtils.class.getName();

    private static final Handler handler = new Handler();
    private static Context context;
    private static Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try {
                UpdateWidget(context);

                if (isUpdating) {
                    handler.postDelayed(this, 1000);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating widget");
                e.printStackTrace();
            } finally {
                //also call the same runnable
                if (isUpdating) {
                    handler.postDelayed(this, 1000);
                }
            }
        }
    };

    public static void BeginUpdating(Context ctx) {
        if (!isUpdating) {
            context = ctx;
            UpdateWidget(ctx);
            //handler.postDelayed(runnable, 1000);
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
