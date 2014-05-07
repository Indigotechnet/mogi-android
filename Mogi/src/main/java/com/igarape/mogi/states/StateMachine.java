package com.igarape.mogi.states;

import android.content.Context;

/**
 * Created by brunosiqueira on 06/05/2014.
 */
public class StateMachine {

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
        if (currentState.equals(state)) {
            return;
        }
        currentState.stop(context);
        currentState = state;
        currentState.start(context);

    }


}
