package com.igarape.mogi.recording;

import android.app.Notification;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.igarape.mogi.R;
import com.igarape.mogi.server.ApiClient;
import com.igarape.mogi.server.AuthenticatedJsonRequest;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.VideoUtils;
import com.igarape.mogi.utils.WidgetUtils;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by felipeamorim on 24/07/2013.
 */
public class StreamingService extends AbstractCameraService implements SurfaceHolder.Callback {
    public static String TAG = "StreamingService";

    private final String serverAddress = "54.221.244.181";

    private Session mSession;

    RequestQueue queue;

    public static long TimeStarted = 0;

    public static boolean IsStreaming = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (VideoUtils.isRecordVideos()){

            if ( IsStreaming ) {
                return START_STICKY;
            }

            Notification notification = new Notification.Builder(this)
                    .setContentTitle("SmartPolicing Streaming")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .build();

            queue = Volley.newRequestQueue(getBaseContext());

            stopService(new Intent(this, RecordingService.class));
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            startForeground(ServiceID, notification);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        mWindowManager.removeView(mSurfaceView);
    }

    @Override
    public void startRecording() {
        if (VideoUtils.isRecordVideos()){
            if ( IsStreaming ) {
                return;
            }

            try {
                mSession = SessionBuilder.getInstance()
                        .setSurfaceView(mSurfaceView)
                        .setContext(getApplicationContext())
                        .setCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
                        .setVideoEncoder(SessionBuilder.VIDEO_H263)
                        .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                        .build();

                mSurfaceView.getHolder().addCallback(this);

                mSession.setDestination(InetAddress.getByName(serverAddress).getHostAddress());
                mSession.getAudioTrack().configure();
                String sdp = mSession.getSessionDescription();
                makeStartStreamingRequest(sdp);
                Log.d("SessionDescription", sdp);
                mSession.start();
                TimeStarted = java.lang.System.currentTimeMillis();
                IsStreaming = true;
                WidgetUtils.BeginUpdating(this);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (RuntimeException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private void makeStartStreamingRequest(String sdp) {
        String url = ApiClient.getServerUrl("/streaming/start");
        JSONObject json = new JSONObject();
        try {
            json.put("sdp", sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(
            Identification.getAccessToken(getBaseContext()), Request.Method.POST, url, json,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.d(TAG, jsonObject.toString());
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                stopRecording();
                Log.d(TAG, "Errror sending stream");
            }
        });

        queue.add(request);
        queue.start();
    }

    private void makeStopStreamingRequest() {
        String url = ApiClient.getServerUrl("/streaming/stop");

        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(
                Identification.getAccessToken(getBaseContext()), Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d(TAG, jsonObject.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(TAG, "Unable to cancel streaming on the server.");
            }
        });

        queue.add(request);
        queue.start();
    }

    @Override
    public void stopRecording() {
        mSession.stop();
        IsStreaming = false;
        makeStopStreamingRequest();
        WidgetUtils.StopUpdating();
    }
}
