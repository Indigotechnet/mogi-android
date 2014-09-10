package com.igarape.mogi.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import com.igarape.mogi.R;
import com.igarape.mogi.manager.MainActivity;
import com.igarape.mogi.pause.CountDownService;
import com.igarape.mogi.recording.ToggleStreamingService;
import com.igarape.mogi.server.AuthenticationActivity;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.UploadProgressUtil;
import com.igarape.mogi.utils.WidgetUtils;

/**
 * Created by felipeamorim on 08/07/2013.
 */
public class MogiAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_main);

            views.setViewVisibility(R.id.widget_streaming_dot, View.GONE);
            PendingIntent mainIntent = null;
            PendingIntent settingsIntent = null;
            // User is not logged
            if (Identification.getAccessToken(context) == null) {
                views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_offline);
                views.setTextViewText(R.id.widget_status_title, "Login");
                views.setTextViewText(R.id.widget_status_info, "");
                views.setTextColor(R.id.widget_status_title, Color.BLACK);
                views.setViewVisibility(R.id.widget_action_button, View.INVISIBLE);
                settingsIntent = PendingIntent.getActivity(context, 0, new Intent(context, AuthenticationActivity.class), 0);
            } else {
                views.setViewVisibility(R.id.widget_action_button, View.VISIBLE);
                Intent actionIntent = null;

                if (StateMachine.getInstance().isInState(State.STREAMING)) {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_pause_button_normal);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_pause);
                    views.setTextViewText(R.id.widget_status_title,  context.getString(R.string.widget_streaming_title));
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_red));
                    views.setTextViewText(R.id.widget_status_info,
                            "Streaming");
                    views.setViewVisibility(R.id.widget_streaming_dot, View.VISIBLE);

                    actionIntent = new Intent(context, ToggleStreamingService.class);
                } else if (StateMachine.getInstance().isInState(State.UPLOADING)) {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_upload_button);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_upload);
                    views.setTextViewText(R.id.widget_status_title, context.getString(R.string.widget_upload_title));
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_blue));
                    views.setTextViewText(R.id.widget_status_info,
                            context.getString(R.string.widget_upload_info_start));
                } else if (StateMachine.getInstance().isInState(State.PAUSED)) {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_paused_button);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_pause);
                    views.setTextViewText(R.id.widget_status_title,  context.getString(R.string.widget_paused_title));
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_orange));
                    views.setTextViewText(R.id.widget_status_info,
                            context.getString(R.string.widget_paused_info));
                } else {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_play_button_normal);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_play);
                    views.setTextViewText(R.id.widget_status_title,  context.getString(R.string.widget_active_title));
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_green));
                    views.setTextViewText(R.id.widget_status_info,
                            context.getString(R.string.widget_logged_time_info, toNowInMinutes(Identification.getTimeLogin(context))));
                    actionIntent = new Intent(context, ToggleStreamingService.class);
                }
                if (actionIntent != null) {
                    mainIntent = PendingIntent.getService(context, appWidgetId, actionIntent, 0);
                }
                settingsIntent = PendingIntent.getActivity(
                        context, appWidgetId, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            }



            if (mainIntent != null) {
                views.setOnClickPendingIntent(R.id.widget_action_bg, mainIntent);
                views.setOnClickPendingIntent(R.id.widget_action_button, mainIntent);
            } else {
                views.setOnClickPendingIntent(R.id.widget_action_bg, null);
                views.setOnClickPendingIntent(R.id.widget_action_button, null);
            }

            views.setOnClickPendingIntent(R.id.widget_settings_button, settingsIntent);


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        WidgetUtils.StopUpdating();
    }

    private String toNowInMinutes(long before) {
        if (before < 0) {
            return "";
        }
        long time = (java.lang.System.currentTimeMillis() - before) / 60000;

        return String.valueOf(time);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);



        if(intent.getAction().contains(UploadProgressUtil.MOGI_UPLOAD_UPDATE)){

            int total = intent.getExtras().getInt(UploadProgressUtil.TOTAL);
            int completed = intent.getExtras().getInt(UploadProgressUtil.COMPLETED);

            RemoteViews views =  new RemoteViews(context.getPackageName(), R.layout.appwidget_main);
            if (total == completed){
                views.setTextViewText(R.id.widget_status_info, context.getString(R.string.upload_progress_finish));
            } else {
                views.setTextViewText(R.id.widget_status_info, context.getString(R.string.upload_progress_info,completed, total));
            }
            ComponentName widget = new ComponentName(context, MogiAppWidgetProvider.class);
            AppWidgetManager.getInstance(context).updateAppWidget(widget, views);
        }
        else if (intent.getAction().equals(CountDownService.MOGI_COUNTDOWN_PAUSE)) {
            RemoteViews views =  new RemoteViews(context.getPackageName(), R.layout.appwidget_main);
            int time = intent.getExtras().getInt("missingTime");

            views.setTextViewText(R.id.widget_status_info, context.getString(R.string.countdown_info,time));
        }

    }
}
