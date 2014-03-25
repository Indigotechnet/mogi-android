package com.igarape.mogi.utils;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by brunosiqueira on 19/02/2014.
 */
public class AlertUtils {
    public static void showAlertDialog(Context context, int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(title)
                .setTitle(message);
        builder.create().show();
    }
}
