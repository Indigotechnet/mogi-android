package com.igarape.mogi.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.NetworkUtils;

/**
 * Created by felipeamorim on 26/07/2013.
 */
public class ConnectivityStatusReceiver extends BroadcastReceiver {
    public static int ServiceID = 1;

    private ConnectivityManager mConnectivityManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (StateMachine.getInstance().isInState(State.NOT_LOGGED)){
            return;
        }
        mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (NetworkUtils.canUpload(context, mConnectivityManager.getActiveNetworkInfo(), intent)) {
            StateMachine.getInstance().startServices(State.UPLOADING, context.getApplicationContext());
        } else {
            boolean hasConnection = NetworkUtils.hasConnection((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            if (hasConnection && !StateMachine.getInstance().isInState(State.STREAMING)) {
                StateMachine.getInstance().startServices(State.RECORDING_ONLINE, context.getApplicationContext());
            } else if (!hasConnection){
                StateMachine.getInstance().startServices(State.RECORDING_OFFLINE, context.getApplicationContext());
            }
        }
    }
}
