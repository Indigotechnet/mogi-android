package com.igarape.mogi.utils;

import android.app.Activity;
import android.util.Log;

import com.igarape.mogi.server.ApiClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by fcavalcanti on 9/5/2014.
 */
public class UserUtils {

    private static final String TAG = UserUtils.class.getName();

    public static void notifyStreamingStart(){

        ApiClient.post("/streams", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                Log.i(TAG, "notifyStreamingStarted sent successfully");
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {//
                Log.i(TAG, "notifyStreamingStarted sent successfully");
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "notifyStreamingStarted not sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "notifyStreamingStarted not sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "notifyStreamingStarted not sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                Log.e(TAG, "notifyStreamingStarted not sent successfully");
            }
        });
    }

    public static void notifyStreamingStop(){

        ApiClient.delete("/streams", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                Log.i(TAG, "notifyStreamingStop sent successfully");
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {//
                Log.i(TAG, "notifyStreamingStop sent successfully");
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "notifyStreamingStop not sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "notifyStreamingStop not sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "notifyStreamingStop not sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                Log.e(TAG, "notifyStreamingStop not sent successfully");
            }
        });
    }
}
