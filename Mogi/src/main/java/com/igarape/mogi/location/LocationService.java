package com.igarape.mogi.location;

import android.app.Notification;
import android.content.Intent;
import android.location.Location;
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

/**
 * Created by felipeamorim on 24/07/2013.
 */
public class LocationService extends BaseService {
    public static String TAG = "LocationService";
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
            request.setInterval(10000);
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

        private void handleLocation(Location location) {
            Log.v(LocationService.TAG, "LocationChanged == @" + location.getLatitude() + "," + location.getLongitude());

            // Parse to file
            FileUtils.LogLocation(location);
            mLastLocation = location;
        }
    }
}
