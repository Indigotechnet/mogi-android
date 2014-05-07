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
            PendingIntent mainIntent;

            // User is not logged
            if (Identification.getAccessToken(context) == null) {
                views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_offline);
                views.setTextViewText(R.id.widget_status_title, "Login");
                views.setTextColor(R.id.widget_status_title, Color.BLACK);
                views.setViewVisibility(R.id.widget_action_button, View.INVISIBLE);
                mainIntent = PendingIntent.getActivity(context, 0, new Intent(context, AuthenticationActivity.class), 0);
            } else {
                views.setViewVisibility(R.id.widget_action_button, View.VISIBLE);
                Intent actionIntent = new Intent(context, ToggleStreamingService.class);
                if (StateMachine.getInstance().isInState(State.STREAMING)) {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_pause_button_normal);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_pause);
                    views.setTextViewText(R.id.widget_status_title, "Streaming");
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_red));
                    views.setTextViewText(R.id.widget_status_info,
                            "Streaming for " + StreamingService.Duration + "minutes");
                    views.setViewVisibility(R.id.widget_streaming_dot, View.VISIBLE);
                } else {
                    views.setInt(R.id.widget_action_bg, "setBackgroundResource", R.drawable.bg_play_button_normal);
                    views.setImageViewResource(R.id.widget_action_button, R.drawable.ic_play);
                    views.setTextViewText(R.id.widget_status_title, "Active");
                    views.setTextColor(R.id.widget_status_title, context.getResources().getColor(R.color.widget_status_green));
                    views.setTextViewText(R.id.widget_status_info,
                            "Logged in for " + toNowInMinutes(Identification.getTimeLogin(context)));
                }

                mainIntent = PendingIntent.getService(context, 0, actionIntent, 0);
            }

            views.setOnClickPendingIntent(R.id.widget_action_bg, mainIntent);
            views.setOnClickPendingIntent(R.id.widget_action_button, mainIntent);

            PendingIntent settingsIntent = PendingIntent.getActivity(
                    context, 0, new Intent(context, MainActivity.class), 0);

            views.setOnClickPendingIntent(R.id.widget_settings_button, settingsIntent);


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private String toNowInMinutes(long before) {
        if (before < 0) {
            return "";
        }
        long time = (java.lang.System.currentTimeMillis() - before) / 60000;

        return String.valueOf(time);
    }

}
