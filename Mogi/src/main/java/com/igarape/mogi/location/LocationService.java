package com.igarape.mogi.location;

import android.app.Notification;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.igarape.mogi.BaseService;
import com.igarape.mogi.R;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.LocationUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by felipeamorim on 24/07/2013.
 */
public class LocationService extends BaseService {
    private static final long INTERVAL = 20000;
    private static final String TAG = LocationService.class.getName();
    public static final int CALL_GPS_INTERVAL = 2000;
    public static final String GPS_PROVIDER = LocationManager.NETWORK_PROVIDER;
    public static int ServiceID = 2;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastKnownLocation;
    private long lastLocationTime;

    @Override
    public void onCreate() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("SmartPolicing Location")
                .setContentText("Recording GPS data")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(ServiceID, notification);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location == null) {
                    Log.v(LocationService.TAG, "onLocationChanged: location == null");
                    return;
                }
                if (mLastKnownLocation == null){
                    mLastKnownLocation = location;
                    lastLocationTime = location.getTime();
                    return;
                }
                if (hasIntervalExpired(location, lastLocationTime)){
                    handleLocation(mLastKnownLocation);
                    mLastKnownLocation = location;
                    lastLocationTime = location.getTime();
                } else if (isBetterLocation(location, mLastKnownLocation)){
                    mLastKnownLocation = location;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        mLastKnownLocation = mLocationManager.getLastKnownLocation(GPS_PROVIDER);
        if (mLastKnownLocation != null){
            handleLocation(mLastKnownLocation);
        }
        // Register the listener with the Location Manager to receive location updates
        mLocationManager.requestLocationUpdates(GPS_PROVIDER, CALL_GPS_INTERVAL, 0, mLocationListener);
    }

    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(mLocationListener);
    }

    private void handleLocation(final Location location) {
        Log.v(LocationService.TAG, "LocationChanged == @" + location.getLatitude() + "," + location.getLongitude());

        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //For 3G check
        boolean is3g = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        //For WiFi Check
        boolean isWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();
        if (is3g || isWifi){
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat(FileUtils.DATE_FORMAT);
            df.setTimeZone(tz);

            try {
                JSONObject locationJson = LocationUtils.buildJson(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), df.format(new Date()));
                LocationUtils.sendLocation(locationJson, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                        Log.i(TAG, "location sent successfully");
                    }

                    @Override
                    public void onFailure(Throwable e, JSONObject errorResponse) {
                        Log.e(TAG, "location not sent successfully");
                        FileUtils.LogLocation(location);
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                        Log.e(TAG, "location not sent successfully", e);
                        FileUtils.LogLocation(location);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                        Log.e(TAG, "location not sent successfully", e);
                        FileUtils.LogLocation(location);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                        Log.e(TAG, "location not sent successfully", e);
                        FileUtils.LogLocation(location);
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "error sending location", e);
                FileUtils.LogLocation(location);
            }
        } else {
            // Parse to file
            FileUtils.LogLocation(location);
        }
    }

    private boolean hasIntervalExpired(Location location, long lastLocationTime){
        long timeDelta = location.getTime() - lastLocationTime;
        return timeDelta > INTERVAL;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -INTERVAL;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
