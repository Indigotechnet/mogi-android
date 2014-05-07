package com.igarape.mogi.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;

public class GcmIntentService extends IntentService {

    private static final String KEY_STREAMING_START = "startStreaming";
    private static final String KEY_STREAMING_STOP = "stopStreaming";
    public static String TAG = GcmIntentService.class.getName();

    public GcmIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d(TAG, extras.toString());

                String key = extras.getString("collapse_key");
                if (KEY_STREAMING_START.equals(key)) {
                    StateMachine.getInstance().startServices(State.STREAMING, getApplicationContext());
                } else {
                    StateMachine.getInstance().startServices(State.RECORDING_ONLINE, getApplicationContext());
                }
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
