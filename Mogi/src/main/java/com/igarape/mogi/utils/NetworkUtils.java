package com.igarape.mogi.utils;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

/**
 * Created by brunosiqueira on 07/05/2014.
 */
public class NetworkUtils {
    public static boolean canUpload(NetworkInfo activeNetwork, Intent intent) {
        if (activeNetwork == null) {
            return false;
        }
        boolean isConnected = activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        return isCharging && isConnected && isWiFi;
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
