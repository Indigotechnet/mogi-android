package com.igarape.mogi.pause;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;

import com.igarape.mogi.BaseService;
import com.igarape.mogi.lock.LockScreenActivity;
import com.igarape.mogi.manager.MainActivity;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.NetworkUtils;

/**
 * Created by brunosiqueira on 30/07/2014.
 */
public class CountDownService extends BaseService {

    public static final String MOGI_COUNTDOWN_PAUSE = "com.igarape.mogi.MOGI_COUNTDOWN_PAUSE";
    public static final String COUNT_DOWN_TIME = "countDownTime";
    private CountDownTimer timer;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        long countDownTime = intent.getExtras().getInt(COUNT_DOWN_TIME);
        timer = new CountDownTimer(countDownTime, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long time = millisUntilFinished / 1000;
                Intent intent = new Intent(getApplicationContext(), LockScreenActivity.class);
                intent.setAction(MOGI_COUNTDOWN_PAUSE);
                intent.putExtra(COUNT_DOWN_TIME, time);

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction(MOGI_COUNTDOWN_PAUSE);
                intent.putExtra(COUNT_DOWN_TIME, time);

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (NetworkUtils.canUpload(getApplicationContext(), mConnectivityManager.getActiveNetworkInfo(), intent)) {
                    StateMachine.getInstance().startServices(State.UPLOADING, getApplicationContext(), null);
                } else {
                    StateMachine.getInstance().startServices(State.RECORDING_ONLINE, getApplicationContext(), null);
                }

                Intent intent = new Intent(getApplicationContext(), LockScreenActivity.class);
                intent.setAction(LockScreenActivity.MOGI_UPDATE_INTERFACE);

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


            }
        };
        timer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
