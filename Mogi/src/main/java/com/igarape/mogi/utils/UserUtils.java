package com.igarape.mogi.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.igarape.mogi.server.ApiClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
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

    public static void applyUserImage(final Activity activity, final ImageView userImage) {
        if (Identification.getUserImage() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

            userImage.setMinimumHeight(dm.heightPixels);
            userImage.setMinimumWidth(dm.widthPixels);
            userImage.setImageBitmap(Identification.getUserImage());
        } else {
            ApiClient.get("/pictures/small/show", null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Bitmap bm = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                    Identification.setUserImage(bm);
                    DisplayMetrics dm = new DisplayMetrics();
                    activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

                    userImage.setMinimumHeight(dm.heightPixels);
                    userImage.setMinimumWidth(dm.widthPixels);
                    userImage.setImageBitmap(bm);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    userImage.setVisibility(View.GONE);
                    super.onFailure(statusCode, headers, responseBody, error);
                }
            });
        }
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
