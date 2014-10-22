package com.igarape.mogi.utils;

import android.location.Location;

import com.igarape.mogi.server.ApiClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by brunosiqueira on 08/03/2014.
 */
public class LocationUtils {

    private static final String TAG = LocationUtils.class.getName();

    public static JSONObject buildJson(Location location) throws JSONException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat(FileUtils.DATE_FORMAT);
        df.setTimeZone(tz);
        return buildJson(location.getLatitude(),location.getLongitude(),df.format(new Date()),
                location.getAccuracy(), location.getExtras() == null ? null : location.getExtras().get("satellites"), location.getProvider(),
                location.getBearing(), location.getSpeed());
    }

    public static JSONObject buildJson(Double latitude, Double longitude, String date,
                                       Float accuracy, Object satellites, String provider,
                                       Float bearing, Float speed) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("lat", latitude);
        json.put("lng", longitude);
        json.put("date", date);

        if (accuracy != null) {
            json.put("accuracy", accuracy);
        }
        if (satellites != null) {
            json.put("satellites", satellites);
        }
        if (provider != null) {
            json.put("provider", provider);
        }
        if (bearing != null) {
            json.put("bearing", bearing);
        }
        if (speed != null) {
            json.put("speed", speed);
        }
        return json;
    }
    /**
     * @deprecated Implement {@link #sendLocations(org.json.JSONArray, String, com.loopj.android.http.JsonHttpResponseHandler)}  instead.
     * */
    public static void sendLocations(JSONArray locations, JsonHttpResponseHandler onSuccess) {
        ApiClient.post("/locations", locations, onSuccess);
    }

    public static void sendLocations(JSONArray locations,String login, JsonHttpResponseHandler onSuccess) {
        ApiClient.post("/locations/" + login, locations, onSuccess);
    }


    public static void sendLocation(JSONObject locationJson, JsonHttpResponseHandler onSuccess) {
        ApiClient.post("/locations", locationJson, onSuccess);
    }

}
