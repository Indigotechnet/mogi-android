package com.igarape.mogi.states;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.igarape.mogi.location.LocationService;
import com.igarape.mogi.recording.RecordingService;
import com.igarape.mogi.recording.StreamingService;
import com.igarape.mogi.server.UploadService;

/**
 * Created by brunosiqueira on 07/05/2014.
 */
public enum State{

    NOT_LOGGED {
        @Override
        public void start(Context context) {

        }

        @Override
        public void stop(Context context) {

        }
    },
    RECORDING_OFFLINE {
        @Override
        public void start(Context context) {
            startSmartPolicingService(LocationService.class, context);
            startSmartPolicingService(RecordingService.class, context);
        }

        @Override
        public void stop(Context context) {
            stopSmartPolicingService(LocationService.class, context);
            stopSmartPolicingService(RecordingService.class, context);
        }
    },
    RECORDING_ONLINE {
        @Override
        public void start(Context context) {
            startSmartPolicingService(LocationService.class, context);
            startSmartPolicingService(RecordingService.class, context);
        }

        @Override
        public void stop(Context context) {
            stopSmartPolicingService(LocationService.class, context);
            stopSmartPolicingService(RecordingService.class, context);
        }
    },
    STREAMING {
        @Override
        public void start(Context context) {
            startSmartPolicingService(LocationService.class, context);
            startSmartPolicingService(StreamingService.class, context);
        }

        @Override
        public void stop(Context context) {
            stopSmartPolicingService(LocationService.class, context);
            stopSmartPolicingService(StreamingService.class, context);
        }
    },
    UPLOADING {
        @Override
        public void start(Context context) {
            startSmartPolicingService(UploadService.class, context);
        }

        @Override
        public void stop(Context context) {
            stopSmartPolicingService(UploadService.class, context);
        }
    };

    public abstract void start(Context context);
    public abstract void stop(Context context);
    private static final String TAG = State.class.getName();

    private static void startSmartPolicingService(final Class clazz,final Context context) {
        try {
            Thread td = new Thread() {
                @Override
                public void run() {
                    if (!isMyServiceRunning(clazz,context)) {
                        context.startService(new Intent(context, clazz));
                    }
                }
            };
            td.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static void stopSmartPolicingService(final Class clazz, Context context) {
        try {
            //Thread td = new Thread() {
            //    @Override
            //    public void run() {
            if (isMyServiceRunning(clazz,context)) {
                context.stopService(new Intent(context, clazz));
            }
            //    }
            //};
            //td.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static boolean isMyServiceRunning(final Class clazz, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (clazz.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}