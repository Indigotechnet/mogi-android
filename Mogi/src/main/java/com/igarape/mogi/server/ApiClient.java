package com.igarape.mogi.server;

import android.content.Context;
import android.util.Log;

import com.igarape.mogi.BuildConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by felipeamorim on 26/08/2013.
 */
public class ApiClient {
    public static final String TAG = ApiClient.class.getName();
    private static final int DEFAULT_TIMEOUT = 30000;
    private static Context appContext;
    private static AsyncHttpClient client;

    static {
        client = new AsyncHttpClient();
        client.setURLEncodingEnabled(true);
        client.setTimeout(DEFAULT_TIMEOUT);
        client.setMaxConnections(5);
        client.addHeader("Content-Type", "multipart/form-data");
    }

    private static String globalToken;

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
            ByteArrayEntity entity = new ByteArrayEntity(body.toString().getBytes("UTF-8"));
            client.post(appContext, getServerUrl(url), entity, "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            responseHandler.onFailure(e, new JSONObject());
        }
    }

    public static void setToken(String token) {
        globalToken = token;
        client.addHeader("Authorization", "Bearer " + token);
    }

    public static String getServerUrl(String path) {
        return String.format("%s%s", BuildConfig.serverUrl, path);
    }

    public static void setAppContext(Context context) {
        appContext = context;
    }

}

