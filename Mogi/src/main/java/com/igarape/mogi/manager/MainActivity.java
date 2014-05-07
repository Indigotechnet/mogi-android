package com.igarape.mogi.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.igarape.mogi.BaseActivity;
import com.igarape.mogi.R;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.server.AuthenticationActivity;
import com.igarape.mogi.server.ConnectivityStatusReceiver;
import com.igarape.mogi.utils.AlertUtils;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.NetworkUtils;
import com.igarape.mogi.utils.WidgetUtils;

public class MainActivity extends BaseActivity {
    public static String TAG = MainActivity.class.getName();

    private BroadcastReceiver connectivityReceiver = null;
    private TextView locationTextView;
    private ConnectivityStatusReceiver connectivityStatusReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Not logged, go to Auth
        if (Identification.getAccessToken(this) == null) {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }
        registerMyReceiver();

        WidgetUtils.UpdateWidget(this.getApplicationContext());
        locationTextView = (TextView) findViewById(R.id.location_status);

        StateMachine.getInstance().startServices(State.RECORDING_ONLINE, getApplicationContext());

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

        ((Button) findViewById(R.id.logout)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                StateMachine.getInstance().startServices(State.NOT_LOGGED, getApplicationContext());
                Identification.setAccessToken(getApplicationContext(), null);
                WidgetUtils.UpdateWidget(MainActivity.this);
                unregisterReceiver(connectivityReceiver);
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (connectivityReceiver == null) {
            connectivityReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    changeLocationStatusGUI();
                }
            };

        }
        registerReceiver(connectivityReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(connectivityReceiver);
        } catch (IllegalArgumentException e) {
            connectivityReceiver = null;
        }
    }

    private void changeLocationStatusGUI() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (NetworkUtils.hasConnection(mConnectivityManager)) {
            locationTextView.setText(getString(R.string.location_status_online));
            locationTextView.setBackgroundColor(Color.GREEN);
        } else {
            locationTextView.setText(getString(R.string.location_status_offline));
            locationTextView.setBackgroundColor(Color.RED);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StateMachine.getInstance().startServices(State.NOT_LOGGED, getApplicationContext());
        try {
            unregisterReceiver(connectivityStatusReceiver);
        } catch (IllegalArgumentException e) {
            connectivityStatusReceiver = null;
        }
    }



    private void registerMyReceiver() {
        IntentFilter mBatteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        connectivityStatusReceiver = new ConnectivityStatusReceiver();
        registerReceiver(connectivityStatusReceiver, mBatteryLevelFilter);
    }

}
