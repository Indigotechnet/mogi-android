package com.igarape.mogi.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.igarape.mogi.lock.LockScreenActivity;
import com.igarape.mogi.widget.MogiAppWidgetProvider;

/**
 * Created by brunosiqueira on 21/05/2014.
 */
public class UploadProgressUtil {

    public static final String MOGI_UPLOAD_UPDATE = "com.igarape.mogi.UPLOAD_UPDATE";

    public static void sendUpdate(Context context, int total, int completed){

        Intent intent = new Intent(context, MogiAppWidgetProvider.class);
        intent.setAction(MOGI_UPLOAD_UPDATE);
        intent.putExtra("total", total);
        intent.putExtra("completed", completed);
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), MogiAppWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);

        intent = new Intent(context, LockScreenActivity.class);
        intent.setAction(MOGI_UPLOAD_UPDATE);
        intent.putExtra("total", total);
        intent.putExtra("completed", completed);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
