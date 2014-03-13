package com.igarape.mogi.recording;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.igarape.mogi.BaseService;
import com.igarape.mogi.utils.VideoUtils;

import net.majorkernelpanic.streaming.gl.SurfaceView;

public abstract class AbstractCameraService extends BaseService implements SurfaceHolder.Callback {
    public static int ServiceID = 3;

    protected WindowManager mWindowManager;
    protected SurfaceView mSurfaceView;
    protected SurfaceHolder mSurfaceHolder;

    public static int Duration = 0;
    public boolean surfaceCreated = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (VideoUtils.isRecordVideos()){
            // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
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
            mSurfaceHolder.setFixedSize(1,1);
        }
        return START_STICKY;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (VideoUtils.isRecordVideos()){
            surfaceCreated = true;
            startRecording();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public abstract void startRecording();

    public abstract void stopRecording();
}
