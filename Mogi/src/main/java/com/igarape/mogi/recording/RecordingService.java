package com.igarape.mogi.recording;

import android.app.Notification;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import com.igarape.mogi.R;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.WidgetUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class RecordingService extends AbstractCameraService implements SurfaceHolder.Callback {
    public static String TAG = "RecordingService";

    private MediaRecorder mMediaRecorder;
    protected Camera mCamera;

    private String mLastFileRecorded;
    public static boolean IsRecording = false;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ( IsRecording ) {
            return START_STICKY;
        }

        Notification notification = new Notification.Builder(this)
                .setContentTitle("SmartPolicing Camera")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(ServiceID, notification);
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        if ( IsRecording ) {
            stopRecording();
        }

        mWindowManager.removeView(mSurfaceView);
    }

    public void startRecording() {
        if (IsRecording) {
            return;
        }

        mCamera = Camera.open(1);
        mCamera.setDisplayOrientation(90);
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        mLastFileRecorded = FileUtils.getPath()+
                DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime())+
                ".mp4";

        mMediaRecorder.setOutputFile(mLastFileRecorded);

        mMediaRecorder.setMaxDuration(600000); // 10mins
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                if ( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ) {
                    stopRecording();
                    startRecording();
                }
            }
        });

        try { mMediaRecorder.prepare(); } catch (Exception e) {
            mCamera.release();
            mCamera = null;
            Log.e(TAG, "Could not prepare media recorder", e);
            return;
        }
        try { mMediaRecorder.start(); } catch (RuntimeException e) {
            mCamera.release();
            mCamera = null;
            Log.e(TAG, "Could not start camera", e);
            return;
        }
        IsRecording = true;
        WidgetUtils.BeginUpdating(this);
    }

    public void stopRecording() {
        if ( !IsRecording) {
            return;
        }

        try {
            mMediaRecorder.stop();
        } catch (RuntimeException e) {
            File lastFile = new File(mLastFileRecorded);
            lastFile.delete();
        }

        mMediaRecorder.reset();
        mMediaRecorder.release();

        mCamera.lock();
        mCamera.release();
        IsRecording = false;
        WidgetUtils.StopUpdating();
    }
}
