package com.igarape.mogi.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.igarape.mogi.BaseActivity;
import com.igarape.mogi.R;
import com.igarape.mogi.location.LocationService;
import com.igarape.mogi.recording.RecordingService;
import com.igarape.mogi.recording.StreamingService;
import com.igarape.mogi.server.AuthenticationActivity;
import com.igarape.mogi.server.UpdateLocationService;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.WidgetUtils;

public class MainActivity extends BaseActivity {
    public static String TAG = "MainActivity";

    private final Class[] activeServices = new Class[] {
        LocationService.class,
        RecordingService.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Not logged, go to Auth
        if (Identification.getAccessToken(this) == null ) {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }

        WidgetUtils.UpdateWidget(this.getApplicationContext());

        startSmartPolicingService(LocationService.class);
        if (!RecordingService.IsRecording && !StreamingService.IsStreaming ) {
            startSmartPolicingService(RecordingService.class);
        }

        ((Button)findViewById(R.id.send_location)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startSmartPolicingService(StreamingService.class);
                startService(new Intent(MainActivity.this, UpdateLocationService.class));
            }
        });

        ((Button)findViewById(R.id.logout)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopServices();
                Identification.setAccessToken(getApplicationContext(), null);
                WidgetUtils.UpdateWidget(MainActivity.this);
                finish();
            }
        });
    }

    private void startServices() {
        for(Class clazz:activeServices) {
            startSmartPolicingService(clazz);
        }
    }

    private void startSmartPolicingService(final Class clazz) {
        try {
            Thread td = new Thread() {
                @Override
                public void run() {
                    startService(new Intent(MainActivity.this, clazz));
                }
            };
            td.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void stopServices() {
        stopSmartPolicingService(LocationService.class);
        stopSmartPolicingService(RecordingService.class);
        stopSmartPolicingService(StreamingService.class);
    }

    private void stopSmartPolicingService(final Class clazz) {
        try {
            //Thread td = new Thread() {
            //    @Override
            //    public void run() {
                    stopService(new Intent(MainActivity.this, clazz));
            //    }
            //};
            //td.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    
}
