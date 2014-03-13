package com.igarape.mogi.utils;

import android.util.Log;

import com.google.gson.JsonObject;
import com.igarape.mogi.server.ApiClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by brunosiqueira on 08/03/2014.
 */
public class LocationUtils {

    private static final String TAG = LocationUtils.class.getName();

    public static JSONObject buildJson(String latitude, String longitude, String date) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("lat", latitude);
        json.put("lng", longitude);
        json.put("date", date);
        return json;
    }

    public static void sendLocations(JSONArray locations, JsonHttpResponseHandler onSuccess) {
        ApiClient.post("/locations", locations, onSuccess);
    }

    public static void sendLocation(JSONObject locationJson, JsonHttpResponseHandler onSuccess) {
        ApiClient.post("/locations",locationJson, onSuccess);
    }
}
