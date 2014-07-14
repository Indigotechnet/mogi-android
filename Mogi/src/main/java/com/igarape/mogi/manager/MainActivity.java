package com.igarape.mogi.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.igarape.mogi.BaseActivity;
import com.igarape.mogi.R;
import com.igarape.mogi.lock.LockScreenReceiver;
import com.igarape.mogi.server.AuthenticationActivity;
import com.igarape.mogi.server.ConnectivityStatusReceiver;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.AlertUtils;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.NetworkUtils;
import com.igarape.mogi.utils.UploadProgressUtil;
import com.igarape.mogi.utils.UserUtils;

public class MainActivity extends BaseActivity {
    public static String TAG = MainActivity.class.getName();

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityStatusReceiver.RECEIVE_NETWORK_UPDATE)) {
                updateLocationStatusGUI();
            }
        }
    };
    private TextView locationTextView;
    private ConnectivityStatusReceiver connectivityStatusReceiver;
    private LockScreenReceiver lockScreenReceiver;
    private LocalBroadcastManager bManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Not logged, go to Auth
        if (Identification.getAccessToken(this) == null) {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }
        defineInitialState();

        bManager = LocalBroadcastManager.getInstance(this);

        registerMyReceivers();

        locationTextView = (TextView) findViewById(R.id.screen_info);

        ImageView userImage = (ImageView) findViewById(R.id.userImage);
        UserUtils.applyUserImage(this, userImage);
        ((TextView)findViewById(R.id.loggedUserLabel)).setText(Identification.getUserName());
        ((Button) findViewById(R.id.force_upload)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
                if (activeNetwork == null) {
                    AlertUtils.showAlertDialog(MainActivity.this, R.string.force_upload, R.string.dialog_upload_no_network);
                    return;
                }
                if (!activeNetwork.isConnectedOrConnecting()) {
                    AlertUtils.showAlertDialog(MainActivity.this, R.string.force_upload, R.string.dialog_upload_no_network);
                    return;
                }

                if (!(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)) {
                    AlertUtils.showAlertDialog(MainActivity.this, R.string.force_upload, R.string.dialog_upload_not_wifi);
                    return;
                }
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = registerReceiver(null, ifilter);

                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                if (!isCharging) {
                    AlertUtils.showAlertDialog(MainActivity.this, R.string.force_upload, R.string.dialog_upload_not_charging);
                    return;
                }

                sendBroadcast(new Intent(MainActivity.this, ConnectivityStatusReceiver.class));
            }
        });

        findViewById(R.id.logout).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                StateMachine.getInstance().startServices(State.NOT_LOGGED, getApplicationContext());
                Identification.setAccessToken(getApplicationContext(), null);
                unregisterMyReceivers();
                finish();
            }
        });
    }

    private void defineInitialState() {
        if (StateMachine.getInstance().isInState(State.NOT_LOGGED)) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (NetworkUtils.canUpload(this, mConnectivityManager.getActiveNetworkInfo(), getIntent())) {
                StateMachine.getInstance().startServices(State.UPLOADING, getApplicationContext());
            } else {
                StateMachine.getInstance().startServices(State.RECORDING_ONLINE, getApplicationContext());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityStatusReceiver.RECEIVE_NETWORK_UPDATE);
        intentFilter.addAction(UploadProgressUtil.MOGI_UPLOAD_UPDATE);
        bManager.registerReceiver(connectivityReceiver, intentFilter);
        updateLocationStatusGUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            bManager.unregisterReceiver(connectivityReceiver);
        } catch (IllegalArgumentException e) {
            connectivityReceiver = null;
        }
    }

    private void updateLocationStatusGUI() {
        if (StateMachine.getInstance().isInState(State.RECORDING_ONLINE)) {
            locationTextView.setText(getString(R.string.location_status_online));
        } else if (StateMachine.getInstance().isInState(State.RECORDING_OFFLINE)) {
            locationTextView.setText(getString(R.string.location_status_offline));
        } else if (StateMachine.getInstance().isInState(State.UPLOADING)) {
            locationTextView.setText(getString(R.string.location_status_uploading));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StateMachine.getInstance().startServices(State.NOT_LOGGED, getApplicationContext());
        unregisterMyReceivers();
    }

    private void unregisterMyReceivers() {
        try {
            unregisterReceiver(lockScreenReceiver);
        } catch (IllegalArgumentException e) {
            lockScreenReceiver = null;
        }
    }


    private void registerMyReceivers() {

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        lockScreenReceiver = new LockScreenReceiver();
        registerReceiver(lockScreenReceiver, filter);
    }


}
