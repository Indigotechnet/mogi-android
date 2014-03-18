package com.igarape.mogi.server;

import android.content.Context;
import android.util.Log;

import com.google.gson.stream.JsonWriter;
import com.igarape.mogi.BuildConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by felipeamorim on 26/08/2013.
 */
public class ApiClient {
    public static final String TAG = ApiClient.class.getName();
    private static Context appContext;
    private static final int DEFAULT_TIMEOUT = 30000;
    private static AsyncHttpClient client;

    static {
        client = new AsyncHttpClient();
        client.setURLEncodingEnabled(true);
        client.setTimeout(DEFAULT_TIMEOUT);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getServerUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getServerUrl(url), params, responseHandler);
    }

    public static void post(String url, JSONObject body, JsonHttpResponseHandler responseHandler) {
        try {
            client.post(appContext, getServerUrl(url), new StringEntity(body.toString()), "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            responseHandler.onFailure(e, new JSONObject());
        }
    }

    public static void post(String url, JSONArray body, JsonHttpResponseHandler responseHandler) {
        try {
            client.post(appContext, getServerUrl(url), new BufferedHttpEntity(new StringEntity(body.toString())), "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            responseHandler.onFailure(e, new JSONObject());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            responseHandler.onFailure(e, new JSONObject());
        }
    }

    public static void setToken(String token) {
        client.addHeader("Authorization", "Bearer " + token);
    }

    public static String getServerUrl(String path) {
        return String.format("%s%s", BuildConfig.serverUrl, path);
    }

    public static void setAppContext(Context context) {
        appContext = context;
    }

}

