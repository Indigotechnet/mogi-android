package com.igarape.mogi.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

import com.igarape.mogi.BuildConfig;

/**
 * Created by brunosiqueira on 07/05/2014.
 */
public class NetworkUtils {
    public static boolean canUpload(Context context, NetworkInfo activeNetwork, Intent intent) {
        if (activeNetwork == null || intent == null) {
            return false;
        }
        boolean isConnected = activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        return isCharging && isConnected && (isWiFi || !BuildConfig.requireWifiUpload);
    }

    public static boolean hasConnection(ConnectivityManager mConnectivityManager) {
        boolean is3g = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        //For WiFi Check
        boolean isWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();

        return is3g || isWifi;
    }
}
