package com.igarape.mogi.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import com.igarape.mogi.R;
import com.igarape.mogi.manager.MainActivity;
import com.igarape.mogi.recording.StreamingService;
import com.igarape.mogi.recording.ToggleStreamingService;
import com.igarape.mogi.server.AuthenticationActivity;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.Identification;
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

            // User is not logged
            if (Identification.getAccessToken(context) == null) {
                views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_offline);
                views.setTextViewText(R.id.widget_status_title, "Login");
                views.setTextColor(R.id.widget_status_title, Color.BLACK);
                views.setViewVisibility(R.id.widget_action_button, View.INVISIBLE);
                mainIntent = PendingIntent.getActivity(context, 0, new Intent(context, AuthenticationActivity.class), 0);
            } else {
                views.setViewVisibility(R.id.widget_action_button, View.VISIBLE);
                Intent actionIntent = null;
                if (StateMachine.getInstance().isInState(State.STREAMING)) {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_pause_button_normal);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_pause);
                    views.setTextViewText(R.id.widget_status_title, "Streaming");
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_red));
                    views.setTextViewText(R.id.widget_status_info,
                            "Streaming for " + StreamingService.Duration + "minutes");
                    views.setViewVisibility(R.id.widget_streaming_dot, View.VISIBLE);

                    actionIntent = new Intent(context, ToggleStreamingService.class);
                } else if (StateMachine.getInstance().isInState(State.UPLOADING)) {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_upload_button);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_upload);
                    views.setTextViewText(R.id.widget_status_title, "Uploading");
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_blue));
                    views.setTextViewText(R.id.widget_status_info,
                            "Uploading files to server");
                } else {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_play_button_normal);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_play);
                    views.setTextViewText(R.id.widget_status_title, "Active");
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_green));
                    views.setTextViewText(R.id.widget_status_info,
                            "Logged in for " + toNowInMinutes(Identification.getTimeLogin(context)));
                    actionIntent = new Intent(context, ToggleStreamingService.class);
                }
                if (actionIntent != null) {
                    mainIntent = PendingIntent.getService(context, appWidgetId, actionIntent, 0);
                }
            }

            PendingIntent settingsIntent = PendingIntent.getActivity(
                    context, appWidgetId, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            if (mainIntent != null) {
                views.setOnClickPendingIntent(R.id.widget_action_bg, mainIntent);
                views.setOnClickPendingIntent(R.id.widget_action_button, mainIntent);
            } else {
                views.setOnClickPendingIntent(R.id.widget_action_bg, settingsIntent);
                views.setOnClickPendingIntent(R.id.widget_action_button, settingsIntent);
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

}
