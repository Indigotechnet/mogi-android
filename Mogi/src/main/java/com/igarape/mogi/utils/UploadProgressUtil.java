package com.igarape.mogi.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.igarape.mogi.lock.LockScreenActivity;
import com.igarape.mogi.manager.MainActivity;

/**
 * Created by brunosiqueira on 21/05/2014.
 */
public class UploadProgressUtil {

    public static final String MOGI_UPLOAD_UPDATE = "com.igarape.mogi.UPLOAD_UPDATE";
    public static final String TOTAL = "total";
    public static final String COMPLETED = "completed";

    public static void sendUpdate(Context context, int total, int completed){

        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.setAction(MOGI_UPLOAD_UPDATE);
        intent.putExtra(TOTAL, total);
        intent.putExtra(COMPLETED, completed);

        intent = new Intent(context, MainActivity.class);
        intent.setAction(MOGI_UPLOAD_UPDATE);
        intent.putExtra(TOTAL, total);
        intent.putExtra(COMPLETED, completed);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
