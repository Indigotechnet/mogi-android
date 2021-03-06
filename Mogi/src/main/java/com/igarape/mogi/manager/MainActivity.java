package com.igarape.mogi.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.igarape.mogi.BaseActivity;
import com.igarape.mogi.BuildConfig;
import com.igarape.mogi.R;
import com.igarape.mogi.lock.LockScreenReceiver;
import com.igarape.mogi.pause.CountDownService;
import com.igarape.mogi.server.AuthenticationActivity;
import com.igarape.mogi.server.ConnectivityStatusReceiver;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.states.StateResponseHandler;
import com.igarape.mogi.utils.AlertUtils;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.NetworkUtils;
import com.igarape.mogi.utils.UploadProgressUtil;
import com.igarape.mogi.utils.UserUtils;

public class MainActivity extends BaseActivity {
    public static final int TEN_MINUTES = 600000;
    public static final int FIVE_MINUTES = 300000;
    public static String TAG = MainActivity.class.getName();

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityStatusReceiver.RECEIVE_NETWORK_UPDATE)) {
                updateState();
                //TODO findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
            } else if (intent.getAction().equals(UploadProgressUtil.MOGI_UPLOAD_UPDATE)) {
                int total = intent.getExtras().getInt(UploadProgressUtil.TOTAL);
                int completed = intent.getExtras().getInt(UploadProgressUtil.COMPLETED);
                TextView uploadInfo = (TextView) findViewById(R.id.screen_info);
                //TODO findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
                if (total == completed){
                    uploadInfo.setText(getString(R.string.upload_progress_finish));
                } else {
                    uploadInfo.setText(getString(R.string.upload_progress_info,completed, total));
                }
            }  else if (intent.getAction().equals(CountDownService.MOGI_COUNTDOWN_PAUSE)) {
                long time = intent.getExtras().getLong(CountDownService.COUNT_DOWN_TIME);

                TextView info = (TextView) findViewById(R.id.screen_info);
                info.setText(getString(R.string.countdown_info,time));
                //TODO findViewById(R.id.pause_button).setVisibility(View.INVISIBLE);
            }
        }
    };
    private TextView locationTextView;
    private ConnectivityStatusReceiver connectivityStatusReceiver;
    private LockScreenReceiver lockScreenReceiver;
    private LocalBroadcastManager bManager;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Not logged, go to Auth
        if (Identification.getAccessToken(this) == null) {

            finish();
        }
        defineInitialState();

        bManager = LocalBroadcastManager.getInstance(this);

        registerMyReceivers();

        locationTextView = (TextView) findViewById(R.id.screen_info);

        ImageView userImage = (ImageView) findViewById(R.id.userImage);
        UserUtils.applyUserImage(this, userImage);
        ((TextView)findViewById(R.id.loggedUserLabel)).setText(Identification.getUserName());
        Button force_upload_button = (Button) findViewById(R.id.force_upload);

        if (BuildConfig.requireWifiUpload){
            force_upload_button.setVisibility(View.INVISIBLE);
        } else {
            force_upload_button.setOnClickListener(new Button.OnClickListener() {
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

                    if (!(NetworkUtils.canUpload(getApplicationContext(), activeNetwork, getIntent()))) {
                        AlertUtils.showAlertDialog(MainActivity.this, R.string.force_upload, R.string.dialog_upload_not_enable);
                        return;
                    }

                    StateMachine.getInstance().startServices(State.UPLOADING, getApplicationContext(), new StateResponseHandler() {
                        @Override
                        public void successResponse() {

                        }

                        @Override
                        public void waitingResponse() {
                            super.waitingResponse();
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.action_waiting_error), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
        final Button pauseButton = (Button) findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow == null) {
                    LayoutInflater layoutInflater
                            = (LayoutInflater) MainActivity.this
                            .getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = layoutInflater.inflate(R.layout.pause_popup, null);
                    popupWindow = new PopupWindow(
                            popupView,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT);

                    ((Button) popupView.findViewById(R.id.cancel_button)).
                            setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    popupWindow.dismiss();
                                }
                            });

                    ((Button) popupView.findViewById(R.id.ten_minutes_button)).
                            setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(CountDownService.COUNT_DOWN_TIME,
                                            TEN_MINUTES);
                                    StateMachine.getInstance().startServices(State.PAUSED, getApplicationContext(), bundle, new StateResponseHandler() {
                                        @Override
                                        public void successResponse() {

                                        }

                                        @Override
                                        public void waitingResponse() {
                                            super.waitingResponse();
                                            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.action_waiting_error), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    popupWindow.dismiss();
                                }
                            });
                    ((Button) popupView.findViewById(R.id.five_minutes_button)).
                            setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(CountDownService.COUNT_DOWN_TIME,
                                            FIVE_MINUTES);
                                    StateMachine.getInstance().startServices(State.PAUSED, getApplicationContext(), bundle, new StateResponseHandler() {
                                        @Override
                                        public void successResponse() {

                                        }

                                        @Override
                                        public void waitingResponse() {
                                            super.waitingResponse();
                                            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.action_waiting_error), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    popupWindow.dismiss();
                                }
                            });
                }
                popupWindow.showAtLocation(findViewById(R.id.main_layout), Gravity.CENTER,0,0);
            }
        });



        findViewById(R.id.logout).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                StateMachine.getInstance().startServices(State.NOT_LOGGED, getApplicationContext(), new StateResponseHandler() {
                    @Override
                    public void successResponse() {
                        Identification.setAccessToken(getApplicationContext(), null);
                        unregisterMyReceivers();
                        finish();
                    }

                    @Override
                    public void waitingResponse() {
                        super.waitingResponse();
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.action_waiting_error), Toast.LENGTH_LONG).show();
                    }
                } );

            }
        });
    }

    private void defineInitialState() {
        if (StateMachine.getInstance().isInState(State.NOT_LOGGED)) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (NetworkUtils.canUpload(this, mConnectivityManager.getActiveNetworkInfo(), getIntent())
                    && BuildConfig.requireWifiUpload) {
                StateMachine.getInstance().startServices(State.UPLOADING, getApplicationContext(),null);
            } else {
                StateMachine.getInstance().startServices(State.RECORDING_ONLINE, getApplicationContext(),null);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityStatusReceiver.RECEIVE_NETWORK_UPDATE);
        intentFilter.addAction(UploadProgressUtil.MOGI_UPLOAD_UPDATE);
        intentFilter.addAction(CountDownService.MOGI_COUNTDOWN_PAUSE);
        bManager.registerReceiver(connectivityReceiver, intentFilter);
        updateState();
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

    private void updateState() {
        if (StateMachine.getInstance().isInState(State.RECORDING_ONLINE)) {
            //TODO findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
            locationTextView.setText(getString(R.string.status_online));
        } else if (StateMachine.getInstance().isInState(State.RECORDING_OFFLINE)) {
            //TODO findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
            locationTextView.setText(getString(R.string.status_offline));
        } else if (StateMachine.getInstance().isInState(State.UPLOADING)) {
            //TODO findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
            locationTextView.setText(getString(R.string.status_uploading));
        } else if (StateMachine.getInstance().isInState(State.PAUSED)) {
            //TODO findViewById(R.id.pause_button).setVisibility(View.INVISIBLE);
            locationTextView.setText(getString(R.string.status_paused));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StateMachine.getInstance().startServices(State.NOT_LOGGED, getApplicationContext(), new StateResponseHandler() {
            @Override
            public void successResponse() {
                startActivity(new Intent(MainActivity.this.getApplicationContext(), AuthenticationActivity.class));
            }

            @Override
            public void waitingResponse() {
                super.waitingResponse();
                Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.action_waiting_error), Toast.LENGTH_LONG).show();
            }
        });

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
