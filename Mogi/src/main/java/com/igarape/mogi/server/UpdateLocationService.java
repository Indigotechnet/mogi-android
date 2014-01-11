package com.igarape.mogi.server;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.igarape.mogi.utils.Identification;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by felipeamorim on 27/08/2013.
 */
public class UpdateLocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    public static String TAG = "UpdateLocationService";

    LocationClient locationClient;
    RequestQueue queue;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationClient = new LocationClient(this, this, this);
        queue = Volley.newRequestQueue(this);

        locationClient.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        queue.stop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location loc = locationClient.getLastLocation();

        if ( loc == null ) {
            return;
        }

        String url = ApiClient.getServerUrl("/locations");
        JSONObject json = new JSONObject();
        try {
            json.put("lat", loc.getLatitude());
            json.put("lng", loc.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(Identification.getAccessToken(this),Request.Method.POST, url, json,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    stopSelf();
                    Log.d(TAG, "Location updated.");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.d(TAG, "Could not update location.");
                    stopSelf();
                }
            }
        );

        queue.add(request);
        queue.start();
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Unable to connect to Location service");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Unable to connect to Location service");
        stopSelf();
    }
}
