package com.igarape.mogi.recording;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;

/**
 * Created by felipeamorim on 19/10/2013.
 */
public class ToggleStreamingService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (StateMachine.getInstance().isInState(State.STREAMING)) {
            StateMachine.getInstance().startServices(State.RECORDING_ONLINE, this.getApplicationContext());
        } else {
            StateMachine.getInstance().startServices(State.STREAMING, this.getApplicationContext());
        }

        stopSelf();
        return START_NOT_STICKY;
    }
}
