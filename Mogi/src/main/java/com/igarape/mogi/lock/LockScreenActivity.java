package com.igarape.mogi.lock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.igarape.mogi.R;
import com.igarape.mogi.server.ConnectivityStatusReceiver;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.util.SystemUiHider;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.UploadProgressUtil;
import com.igarape.mogi.utils.UserUtils;

import java.io.InputStream;
import java.net.URL;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LockScreenActivity extends Activity {

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityStatusReceiver.RECEIVE_NETWORK_UPDATE)) {
                updateState();
            } else if (intent.getAction().equals(UploadProgressUtil.MOGI_UPLOAD_UPDATE)) {
                int total = intent.getExtras().getInt("total");
                int completed = intent.getExtras().getInt("completed");
                TextView uploadInfo = (TextView) findViewById(R.id.lock_screen_info);
                if (total == completed){
                    uploadInfo.setText(getString(R.string.upload_progress_finish));
                } else {
                    uploadInfo.setText(getString(R.string.upload_progress_info,completed, total));
                }
            }
        }
    };
    private LocalBroadcastManager bManager;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityStatusReceiver.RECEIVE_NETWORK_UPDATE);
        intentFilter.addAction(UploadProgressUtil.MOGI_UPLOAD_UPDATE);
        bManager.registerReceiver(bReceiver, intentFilter);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


        setContentView(R.layout.activity_lock_screen);
        ImageView userImage = (ImageView) findViewById(R.id.userImage);
        UserUtils.applyUserImage(this, userImage);

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

        Switch streamToogle = (Switch) findViewById(R.id.streamToogle);
        streamToogle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    StateMachine.getInstance().startServices(State.STREAMING,getApplicationContext());
                } else {
                    StateMachine.getInstance().startServices(State.RECORDING_ONLINE,getApplicationContext());
                }
                updateState();
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



    private static Drawable getImageFromWeb(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        ImageView headquarter = (ImageView) findViewById(R.id.headquarterImage);
        ImageView linkImage = (ImageView) findViewById(R.id.linkImage);
        Switch streamToogle = (Switch) findViewById(R.id.streamToogle);
        TextView uploadInfo = (TextView) findViewById(R.id.lock_screen_info);

        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        RelativeLayout mainUploadingLayout = (RelativeLayout) findViewById(R.id.mainUploadingLayout);
        uploadInfo.setText("");
        TextView text = (TextView) findViewById(R.id.lockTextState);
        if (StateMachine.getInstance().isInState(State.RECORDING_OFFLINE)){
            mainLayout.setVisibility(View.VISIBLE);
            mainUploadingLayout.setVisibility(View.INVISIBLE);
            streamToogle.setClickable(false);
            streamToogle.setChecked(false);

            headquarter.setImageResource(R.drawable.headquarters_grey);
            linkImage.setImageResource(R.drawable.link_grey);
            text.setText(getString(R.string.lock_text_recording_offline));
        } else if (StateMachine.getInstance().isInState(State.STREAMING)){
            mainLayout.setVisibility(View.VISIBLE);
            mainUploadingLayout.setVisibility(View.INVISIBLE);
            streamToogle.setClickable(true);
            streamToogle.setChecked(true);

            headquarter.setImageResource(R.drawable.headquarters_white);
            linkImage.setImageResource(R.drawable.link_white);
            text.setText(getString(R.string.lock_text_livestreaming));
        } else if (StateMachine.getInstance().isInState(State.UPLOADING)){
            mainLayout.setVisibility(View.INVISIBLE);
            mainUploadingLayout.setVisibility(View.VISIBLE);
            streamToogle.setClickable(true);
            streamToogle.setChecked(false);

        } else {
            mainLayout.setVisibility(View.VISIBLE);
            mainUploadingLayout.setVisibility(View.INVISIBLE);
            streamToogle.setClickable(true);
            streamToogle.setChecked(false);

            headquarter.setImageResource(R.drawable.headquarters_white);
            linkImage.setImageResource(R.drawable.link_grey);
            text.setText(getString(R.string.lock_text_recording));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateState();
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


            return true;
        }
        return false;
    }


    public void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }
}
