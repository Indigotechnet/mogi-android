package com.igarape.mogi.utils;

import android.util.Log;

import com.igarape.mogi.server.ApiClient;
import com.igarape.mogi.states.State;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by brunosiqueira on 23/09/2014.
 */
public class HistoryUtils {


    private static final String TAG = HistoryUtils.class.getName();

    public static JSONObject buildJson(State currentState, State nextState) throws JSONException {
        JSONObject json = new JSONObject();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat(FileUtils.DATE_FORMAT);
        df.setTimeZone(tz);
        json.put("previousState", currentState.toString());
        json.put("nextState", nextState.toString());
        json.put("date", df.format(new Date()));
        return json;
    }

    public static void registerHistory(State currentState, State nextState) {

        try {
            final JSONObject history = buildJson(currentState, nextState);
            ApiClient.post("/histories", history, new JsonHttpResponseHandler(){
                @Override
                public void onFailure(Throwable e, JSONObject errorResponse) {
                    Log.e(TAG, "history not sent successfully");
                    FileUtils.LogHistory(history);
                }

                @Override
                public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                    Log.e(TAG, "history not sent successfully", e);
                    FileUtils.LogHistory(history);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    Log.e(TAG, "history not sent successfully", e);
                    FileUtils.LogHistory(history);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                    Log.e(TAG, "history not sent successfully", e);
                    FileUtils.LogHistory(history);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendHistories(JSONArray histories, JsonHttpResponseHandler onSuccess) {
        ApiClient.post("/histories", histories, onSuccess);
    }
}
