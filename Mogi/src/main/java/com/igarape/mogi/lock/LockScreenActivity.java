package com.igarape.mogi.lock;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.igarape.mogi.R;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.util.SystemUiHider;
import com.igarape.mogi.utils.Identification;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LockScreenActivity extends Activity {

//    @Override
//    public void onAttachedToWindow() {
//        // TODO Auto-generated method stub
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//
//        super.onAttachedToWindow();
//    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


        setContentView(R.layout.activity_lock_screen);
        TextView loggedUserLabel = (TextView) findViewById(R.id.loggedUserLabel);
        loggedUserLabel.setText(Identification.getUserName());

        Switch switchUnlock = (Switch) findViewById(R.id.switchUnlock);
        switchUnlock.setChecked(false);
        switchUnlock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    LockScreenActivity.this.finish();
                }
            }
        });

        ImageView icon = (ImageView) findViewById(R.id.lockScreenIcon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StateMachine.getInstance().isInState(State.RECORDING_ONLINE)){
                    StateMachine.getInstance().startServices(State.STREAMING, getApplicationContext());
                    updateState();
                } else if (StateMachine.getInstance().isInState(State.STREAMING)){
                    StateMachine.getInstance().startServices(State.RECORDING_ONLINE, getApplicationContext());
                    updateState();
                }
            }
        });

        if (getIntent() != null && getIntent().hasExtra("kill") && getIntent().getExtras().getInt("kill") == 1) {
            finish();
        }

        try {
            StateListener phoneStateListener = new StateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        ImageView icon = (ImageView) findViewById(R.id.lockScreenIcon);
        TextView text = (TextView) findViewById(R.id.lockTextState);
        if (StateMachine.getInstance().isInState(State.RECORDING_OFFLINE)){
            icon.setImageResource(R.drawable.launcher_recording_off);
            text.setText(getString(R.string.lock_text_recording_offline));
        } else if (StateMachine.getInstance().isInState(State.STREAMING)){
            icon.setImageResource(R.drawable.launcher_streaming);
            text.setText(getString(R.string.lock_text_livestreaming));
        } else if (StateMachine.getInstance().isInState(State.UPLOADING)){
            icon.setImageResource(R.drawable.launcher_uploading);
            text.setText(getString(R.string.lock_text_uploading));
        } else {
            icon.setImageResource(R.drawable.launcher_recording_on);
            text.setText(getString(R.string.lock_text_recording));
        }
    }

    class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("call Activity off hook");
                    finish();


                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
        return;
    }

    //only used in lockdown mode
    @Override
    protected void onPause() {
        super.onPause();

        // Don't hang around.
        // finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Don't hang around.
        // finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_POWER) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
            //this is where I can do my stuff
            return true; //because I handled the event
        }
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {

            return true;
        }

        return false;

    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {

            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

            System.out.println("alokkkkkkkkkkkkkkkkk");
            return true;
        }
        return false;
    }


    public void onDestroy() {
        super.onDestroy();
    }
}