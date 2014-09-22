package com.igarape.mogi.states;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.igarape.mogi.BaseService;
import com.igarape.mogi.location.LocationService;
import com.igarape.mogi.pause.CountDownService;
import com.igarape.mogi.recording.RecordingService;
import com.igarape.mogi.recording.StreamingService;
import com.igarape.mogi.server.UploadService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by brunosiqueira on 07/05/2014.
 */
public enum State {

    NOT_LOGGED {
        @Override
        protected java.util.List<Class<? extends BaseService>> getServices() {
            return new ArrayList<Class<? extends BaseService>>();
        }

    },
    PAUSED {
        @Override
        protected java.util.List<Class<? extends BaseService>> getServices() {
            return Arrays.asList(LocationService.class, CountDownService.class);
        }
    },
    RECORDING_OFFLINE {
        @Override
        public boolean isWaitToBeReady() {
            return true;
        }

        @Override
        protected java.util.List<Class<? extends BaseService>> getServices() {
            return Arrays.asList(LocationService.class, RecordingService.class);
        }

    },
    RECORDING_ONLINE {
        @Override
        public boolean isWaitToBeReady() {
            return true;
        }

        @Override
        protected java.util.List<Class<? extends BaseService>> getServices() {
            return Arrays.asList(LocationService.class, RecordingService.class);
        }

    },
    STREAMING {
        @Override
        public boolean isWaitToBeReady() {
            return true;
        }

        @Override
        protected java.util.List<Class<? extends BaseService>> getServices() {
            return Arrays.asList(LocationService.class, StreamingService.class);
        }

    },
    UPLOADING {
        @Override
        protected java.util.List<Class<? extends BaseService>> getServices() {
            List list = new ArrayList<Class<? extends BaseService>>();
            list.add(UploadService.class);
            return list;
        }
    };
    private static final String TAG = State.class.getName();

    private boolean waitToBeReady = false;
    private static void startSmartPolicingService(final Class clazz, final Context context, final Bundle extras) {
        try {
            final Thread td = new Thread() {
                @Override
                public void run() {
                    if (!isMyServiceRunning(clazz, context)) {
                        Intent intent = new Intent(context, clazz);
                        if (extras != null) {
                            intent.putExtras(extras);
                        }
                        context.startService(intent);
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
            if (isMyServiceRunning(clazz, context)) {
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


    protected abstract java.util.List<Class<? extends BaseService>> getServices();

    protected void start(Context context, Bundle extras){
        for (Class<? extends BaseService> service: getServices()){
            startSmartPolicingService(service, context, extras);
        }
    };

    protected void stop(Context context, State newState){
        for (Class<? extends BaseService> service: getServices()){

            if (!newState.getServices().contains(service)) {
                stopSmartPolicingService(service, context);
            }
        }
    };

    public boolean isWaitToBeReady() {
        return waitToBeReady;
    }

}