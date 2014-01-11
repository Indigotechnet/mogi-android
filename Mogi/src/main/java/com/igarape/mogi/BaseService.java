package com.igarape.mogi;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.igarape.mogi.MogiApp;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public abstract class BaseService extends Service {
    @Inject protected Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();

        ((MogiApp)getApplication()).objectGraph().inject(this);
        bus.register(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);

        super.onDestroy();
    }
}
