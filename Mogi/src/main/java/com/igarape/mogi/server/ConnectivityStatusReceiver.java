package com.igarape.mogi.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.igarape.mogi.R;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.states.StateResponseHandler;

/**
 * Created by felipeamorim on 26/07/2013.
 */
public class ConnectivityStatusReceiver extends BroadcastReceiver {

    public static final String RECEIVE_NETWORK_UPDATE = "com.igarape.mogi.server.NETWORK_UPDATE";
    public static int ServiceID = 1;

    private ConnectivityManager mConnectivityManager;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (StateMachine.getInstance().isInState(State.NOT_LOGGED) ||
                StateMachine.getInstance().isInState(State.PAUSED)){
            return;
        }
        StateMachine.goToActiveState(context, intent, new StateResponseHandler() {
            @Override
            public void successResponse() {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RECEIVE_NETWORK_UPDATE));
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RECEIVE_NETWORK_UPDATE));
            }

            @Override
            public void waitingResponse() {
                super.waitingResponse();
                Toast.makeText(context, context.getString(R.string.action_waiting_error), Toast.LENGTH_LONG).show();

            }
        });

    }


}
