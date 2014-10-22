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
import com.igarape.mogi.BaseService;
import com.igarape.mogi.R;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.Identification;
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
    public static final int CALL_GPS_INTERVAL = 1000;
    private static final long INTERVAL = 10000;
    private static final String TAG = LocationService.class.getName();
    public static int ServiceID = 2;

    private Location mLastKnownLocation;
    private long lastLocationTime;

    private LocationClient mLocationClient;
    private LocationCallback mLocationCallback;
    private String mUserLogin;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notification_location_title))
                .setContentText(getString(R.string.notification_location_description))
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(ServiceID, notification);
        mUserLogin = Identification.getUserLogin(this);
        if (mLocationClient == null) {
            mLocationCallback = new LocationCallback();
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

    private boolean hasIntervalExpired(Location location, long lastLocationTime) {
        long timeDelta = location.getTime() - lastLocationTime;
        return timeDelta > INTERVAL;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
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

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
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
            if (StateMachine.getInstance().isInState(State.RECORDING_ONLINE) || StateMachine.getInstance().isInState(State.RECORDING_OFFLINE)) {
                LocationRequest request = LocationRequest.create();
                request.setFastestInterval(CALL_GPS_INTERVAL);
                request.setInterval(CALL_GPS_INTERVAL * 2);
                request.setSmallestDisplacement(0);
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationClient.requestLocationUpdates(request, mLocationCallback);
            }
        }

        @Override
        public void onDisconnected() {
            Log.v(LocationService.TAG, "Location Client disconnected by the system");
            stopSelf();
        }

        @Override
        public void onLocationChanged(Location location) {


            // Called when a new location is found by the network location provider.
            if (location == null) {
                Log.v(LocationService.TAG, "onLocationChanged: location == null");
                return;
            }
            if (mLastKnownLocation == null) {
                mLastKnownLocation = location;
                lastLocationTime = location.getTime();
                return;
            }
            if (hasIntervalExpired(location, lastLocationTime)) {
                handleLocation(mLastKnownLocation);
                mLastKnownLocation = location;
                lastLocationTime = location.getTime();
            } else if (isBetterLocation(location, mLastKnownLocation)) {
                mLastKnownLocation = location;
            }
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
            if (is3g || isWifi) {

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat(FileUtils.DATE_FORMAT);
                df.setTimeZone(tz);
                try {
                    JSONObject locationJson = LocationUtils.buildJson(location.getLatitude(), location.getLongitude(), df.format(new Date()),
                            location.getAccuracy(), location.getExtras() == null ? null : location.getExtras().get("satellites"),
                            location.getProvider(), location.getBearing(), location.getSpeed());
                    LocationUtils.sendLocation(locationJson, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                            Log.i(TAG, "location sent successfully");
                        }

                        @Override
                        public void onFailure(Throwable e, JSONObject errorResponse) {
                            Log.e(TAG, "location not sent successfully");
                            FileUtils.LogLocation(mUserLogin, location);
                        }

                        @Override
                        public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                            Log.e(TAG, "location not sent successfully", e);
                            FileUtils.LogLocation(mUserLogin, location);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                            Log.e(TAG, "location not sent successfully", e);
                            FileUtils.LogLocation(mUserLogin, location);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                            Log.e(TAG, "location not sent successfully", e);
                            FileUtils.LogLocation(mUserLogin, location);
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "error sending location", e);
                    FileUtils.LogLocation(mUserLogin, location);
                }
            } else {
                // Parse to file
                FileUtils.LogLocation(mUserLogin, location);
            }
        }
    }
}
