package com.igarape.mogi.states;

import android.util.Log;

/**
 * Created by brunosiqueira on 17/09/2014.
 */
public abstract class StateResponseHandler {

    private static final String TAG = StateResponseHandler.class.getName();

    public void successResponse(){};

    public void waitingResponse(){
        Log.e(TAG, "state is not ready: "+StateMachine.getInstance().getCurrentState());
    };
}
