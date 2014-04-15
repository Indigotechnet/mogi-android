package com.igarape.mogi.recording;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by felipeamorim on 19/10/2013.
 */
public class ToggleStreamingService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (StreamingService.IsStreaming) {
            stopService(new Intent(this, StreamingService.class));
            startService(new Intent(this, RecordingService.class));
        } else {
            stopService(new Intent(this, RecordingService.class));
            startService(new Intent(this, StreamingService.class));
        }

        stopSelf();
        return START_NOT_STICKY;
    }
}
