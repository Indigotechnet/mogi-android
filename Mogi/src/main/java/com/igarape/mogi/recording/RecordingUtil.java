package com.igarape.mogi.recording;

import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;

/**
 * Created by brunosiqueira on 18/02/2014.
 */
public class RecordingUtil {
    public static boolean isInAction() {
        return StateMachine.getInstance().isInState(State.STREAMING) || StateMachine.getInstance().isInState(State.RECORDING_ONLINE);
    }
}
