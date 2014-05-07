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
        mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (NetworkUtils.canUpload(mConnectivityManager.getActiveNetworkInfo(), intent)) {
            StateMachine.getInstance().startServices(State.UPLOADING, context.getApplicationContext());
        } else if (NetworkUtils.hasConnection((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))) {
            StateMachine.getInstance().startServices(State.RECORDING_ONLINE, context.getApplicationContext());
        } else {
            StateMachine.getInstance().startServices(State.RECORDING_OFFLINE, context.getApplicationContext());
        }
    }
}
