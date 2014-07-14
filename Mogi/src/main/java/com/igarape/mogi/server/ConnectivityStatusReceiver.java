package com.igarape.mogi.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.content.LocalBroadcastManager;

import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.NetworkUtils;

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
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RECEIVE_NETWORK_UPDATE));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(RECEIVE_NETWORK_UPDATE));
    }
}
