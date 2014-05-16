package com.igarape.mogi.recording;

import android.app.Notification;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import com.igarape.mogi.R;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.VideoUtils;

import java.io.File;
import java.util.Date;

public class RecordingService extends AbstractCameraService implements SurfaceHolder.Callback {
    public static String TAG = RecordingService.class.getName();
    private static boolean IsRecording = false;
    protected Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private String mLastFileRecorded;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (VideoUtils.isRecordVideos()) {
            if (IsRecording) {
                return START_STICKY;
            }

            Notification notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.notification_record_title))
                    .setContentText(getString(R.string.notification_record_description))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .build();

            startForeground(ServiceID, notification);
            return super.onStartCommand(intent, flags, startId);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (VideoUtils.isRecordVideos()) {
            if (IsRecording) {
                stopRecording();
            }

            mWindowManager.removeView(mSurfaceView);
        }
    }

    @Override
    public void startRecording() {
        if (VideoUtils.isRecordVideos()) {
            if (IsRecording) {
                return;
            }

            mCamera = Camera.open(0);

            mCamera.setDisplayOrientation(VideoUtils.DEGREES);
            mMediaRecorder = new MediaRecorder();

            mCamera.unlock();
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mMediaRecorder.setOrientationHint(VideoUtils.DEGREES);
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
            mMediaRecorder.setVideoFrameRate(15);


            mLastFileRecorded = FileUtils.getPath() +
                    DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime()) +
                    ".mp4";

            mMediaRecorder.setOutputFile(mLastFileRecorded);

            mMediaRecorder.setMaxDuration(VideoUtils.MAX_DURATION_MS);
            mMediaRecorder.setMaxFileSize(VideoUtils.MAX_SIZE_BYTES);
            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                            what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                        stopRecording();
                        startRecording();
                    }
                }
            });

            try {
                mMediaRecorder.prepare();
            } catch (Exception e) {
                mCamera.release();
                mCamera = null;
                Log.e(TAG, "Could not prepare media recorder", e);
                return;
            }
            try {
                mMediaRecorder.start();
            } catch (RuntimeException e) {
                mCamera.release();
                mCamera = null;
                Log.e(TAG, "Could not start camera", e);
                return;
            }
            IsRecording = true;

        }
    }

    @Override
    public void stopRecording() {
        if (VideoUtils.isRecordVideos()) {
            if (!IsRecording) {
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
        }
    }
}
