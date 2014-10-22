package com.igarape.mogi.recording;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.states.StateResponseHandler;

/**
 * Created by felipeamorim on 19/10/2013.
 */
public class ToggleStreamingService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        final Context context = this.getApplicationContext();
        if (StateMachine.getInstance().isInState(State.STREAMING)) {
            StateMachine.getInstance().startServices(State.RECORDING_ONLINE, context, new StateResponseHandler() {
                @Override
                public void successResponse() {

                }

                @Override
                public void waitingResponse() {
                    super.waitingResponse();
                }
            });
        } else {
            StateMachine.getInstance().startServices(State.STREAMING, context, new StateResponseHandler() {
                @Override
                public void successResponse() {

                }

                @Override
                public void waitingResponse() {
                    super.waitingResponse();
                }
            });
        }

        stopSelf();
        return START_NOT_STICKY;
    }
}
