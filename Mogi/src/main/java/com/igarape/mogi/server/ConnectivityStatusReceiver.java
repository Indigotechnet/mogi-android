package com.igarape.mogi.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.content.LocalBroadcastManager;

import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;

/**
 * Created by felipeamorim on 26/07/2013.
 */
public class ConnectivityStatusReceiver extends BroadcastReceiver {
    //    @Override
    //    public void onAttachedToWindow() {
    //        // TODO Auto-generated method stub
    //        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
    //
    //        super.onAttachedToWindow();
    //    }
    public static final String RECEIVE_NETWORK_UPDATE = "com.igarape.mogi.server.NETWORK_UPDATE";
    public static int ServiceID = 1;

    private ConnectivityManager mConnectivityManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (StateMachine.getInstance().isInState(State.NOT_LOGGED) ||
                StateMachine.getInstance().isInState(State.PAUSED)){
            return;
        }
        StateMachine.goToActiveState(context, intent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RECEIVE_NETWORK_UPDATE));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RECEIVE_NETWORK_UPDATE));
    }


}
