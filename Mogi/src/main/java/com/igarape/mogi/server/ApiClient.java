package com.igarape.mogi.server;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.igarape.mogi.widget.MogiAppWidgetProvider;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by felipeamorim on 26/08/2013.
 */
public class ApiClient {

    private final static String serverUrl = "http://projectmogi.com";

    private static Context appContext;

    private static AsyncHttpClient client = new AsyncHttpClient();

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
            Log.e("ApiClient", e.getMessage());
            responseHandler.onFailure(e, new JSONObject());
        }
    }

    public static void post(String url, JSONArray body, JsonHttpResponseHandler responseHandler) {
        try {
            client.post(appContext, getServerUrl(url), new StringEntity(body.toString()), "application/json", responseHandler);
        } catch (UnsupportedEncodingException e) {
            Log.e("ApiClient", e.getMessage());
            responseHandler.onFailure(e, new JSONObject());
        }
    }

    public static void setToken(String token) {
        client.addHeader("Authorization", token);
    }

    public static String getServerUrl(String path) {
        return String.format("%s%s", serverUrl, path);
    }

    public static void setAppContext(Context context) {
        appContext = context;
    }

}

