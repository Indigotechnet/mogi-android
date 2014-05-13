package com.igarape.mogi.recording;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.igarape.mogi.BaseService;
import com.igarape.mogi.R;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.UserUtils;
import com.igarape.mogi.utils.VideoUtils;
import com.igarape.mogi.utils.WidgetUtils;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;


/**
 * Created by felipeamorim on 24/07/2013.
 */
public class StreamingService extends BaseService implements RtspClient.Callback, Session.Callback, SurfaceHolder.Callback {
    public static String TAG = StreamingService.class.getName();
    public static int Duration = 0;
    private static Session mSession;
    private static RtspClient mClient;
    private static boolean IsStreaming = false;
    private SurfaceView mSurfaceView;
    private WindowManager mWindowManager;
    private int ServiceID = 5;
    private SurfaceHolder mSurfaceHolder;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (IsStreaming) {
            return START_STICKY;
        }

        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        mSurfaceView = new SurfaceView(this, null);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(mSurfaceView, layoutParams);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("SmartPolicing Streaming")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(ServiceID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClient.stopStream();
        mClient.release();
        mSession.release();
        IsStreaming = false;
        WidgetUtils.StopUpdating();

        /**
         * Here we`ll tell node(server) that user stopped streaming
         */
        UserUtils.notifyStreamingStop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        SessionBuilder builder = SessionBuilder.getInstance()
                .setCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(8000, 16000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(VideoUtils.DEGREES)
                .setCallback(this);

        if (android.os.Build.VERSION.SDK_INT <= 16) {
            builder = builder.setVideoQuality(new VideoQuality(176, 144, 15, 500000));
        }
        // Configures the SessionBuilder
        mSession = builder.setCallback(this)
                .build();


        // Configures the RTSP client
        mClient = new RtspClient();

        mClient.setCredentials(Identification.getStreamingUser(), Identification.getStreamingPassword());
        mClient.setServerAddress(Identification.getServerIpAddress(), Identification.getStreamingPort());
        mClient.setStreamPath(Identification.getStreamingPath());


        mClient.setSession(mSession);
        mClient.setCallback(this);

        mSession.startPreview();
        mClient.startStream();
        IsStreaming = true;
        WidgetUtils.BeginUpdating(this);

        /**
         * Here we`ll tell node(server) that user is streaming
         */
        UserUtils.notifyStreamingStart();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onBitrareUpdate(long bitrate) {
        Log.i(TAG, bitrate / 1000 + " kbps");
    }


    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        Log.e(TAG, "On Session Error", e);
    }

    @Override
    public void onPreviewStarted() {

    }

    @Override
    public void onSessionConfigured() {

    }

    @Override
    public void onSessionStarted() {

    }

    @Override
    public void onSessionStopped() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRtspUpdate(int message, Exception e) {
        Log.e(TAG, "RTSP update", e);
    }
}
