package com.igarape.mogi.states;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.igarape.mogi.server.ApiClient;
import com.igarape.mogi.utils.NetworkUtils;
import com.igarape.mogi.utils.WidgetUtils;
import com.loopj.android.http.RequestParams;

/**
 * Created by brunosiqueira on 06/05/2014.
 */
public class StateMachine {

    private static final String TAG = StateMachine.class.getName();
    private static StateMachine _instance;
    private State currentState;

    private StateMachine() {
        currentState = State.NOT_LOGGED;
    }

    public synchronized static StateMachine getInstance() {
        if (_instance == null) {
            _instance = new StateMachine();
        }
        return _instance;
    }

    public boolean isInState(State state) {
        return currentState.equals(state);
    }

    public synchronized void startServices(State state, Context context) {
        startServices(state, context, null);
    }

    public synchronized void startServices(State state, Context context, Bundle extras) {
        if (currentState.equals(state)) {
            return;
        }
        RequestParams params = new RequestParams();
        params.add("previousState", currentState.toString());
        params.add("nextState", state.toString());
        ApiClient.post("/histories", params);
        currentState.stop(context, state);
        currentState = state;
        currentState.start(context, extras);
        WidgetUtils.BeginUpdating(context);
    }

    public static void goToActiveState(Context context, Intent intent) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (NetworkUtils.canUpload(context, mConnectivityManager.getActiveNetworkInfo(), intent)) {
            getInstance().startServices(State.UPLOADING, context.getApplicationContext());
        } else {
            boolean hasConnection = NetworkUtils.hasConnection((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            if (hasConnection && !getInstance().isInState(State.STREAMING)) {
                getInstance().startServices(State.RECORDING_ONLINE, context.getApplicationContext());
            } else if (!hasConnection){
                getInstance().startServices(State.RECORDING_OFFLINE, context.getApplicationContext());
            }
        }
    }
}
