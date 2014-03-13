package com.igarape.mogi.location;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.igarape.mogi.R;
import com.igarape.mogi.BaseService;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.LocationUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by felipeamorim on 24/07/2013.
 */
public class LocationService extends BaseService {
    public static final int INTERVAL = 20000;
    public static String TAG = LocationService.class.getName();
    public static int ServiceID = 2;

    private LocationClient mLocationClient;
    private LocationCallback mLocationCallback = new LocationCallback();
    private Location mLastLocation;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("SmartPolicing Location")
                .setContentText("Recording GPS data")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(ServiceID, notification);

        if (mLocationClient == null) {
            mLocationClient = new LocationClient(this, mLocationCallback, mLocationCallback);
            Log.v(LocationService.TAG, "Location Client connect");
            if (!(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
                mLocationClient.connect();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationClient.disconnect();
        }
    }

    private class LocationCallback implements
            GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener,
            LocationListener {

        @Override
        public void onConnected(Bundle bundle) {
            Log.v(LocationService.TAG, "Location Client connected");

            // Display last location
            Location location = mLocationClient.getLastLocation();
            if (location != null) {
                handleLocation(location);
            }

            // Request for location updates
            LocationRequest request = LocationRequest.create();
            request.setInterval(INTERVAL);
            request.setSmallestDisplacement(1);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationClient.requestLocationUpdates(request, mLocationCallback);
        }

        @Override
        public void onDisconnected() {
            Log.v(LocationService.TAG, "Location Client disconnected by the system");
            stopSelf();
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                Log.v(LocationService.TAG, "onLocationChanged: location == null");
                return;
            }

            if (mLastLocation != null &&
                    mLastLocation.getLatitude() == location.getLatitude() &&
                    mLastLocation.getLongitude() == location.getLongitude()) {
                return;
            }

            handleLocation(location);
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.v(LocationService.TAG, "Location Client connection failed");
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
            mLastLocation = location;
        }
    }
}
