package com.igarape.mogi.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.igarape.mogi.utils.WidgetUtils;

/**
 * Created by felipeamorim on 19/10/2013.
 */
public class WidgetUpdateService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WidgetUtils.UpdateWidget(this);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
