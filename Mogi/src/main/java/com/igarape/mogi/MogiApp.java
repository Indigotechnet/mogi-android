package com.igarape.mogi;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.igarape.mogi.server.ApiClient;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.WidgetUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.io.File;

import javax.inject.Singleton;

import dagger.ObjectGraph;
import dagger.Provides;

/**
 * Created by felipeamorim on 08/07/2013.
 */
public class MogiApp extends Application {
    public static String TAG = MogiApp.class.getName();
    private ObjectGraph objectGraph;
    private Bus mBus;

    public static final String SENDER_ID = "319635303076";

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new MogiModule(this));
        mBus = new Bus(ThreadEnforcer.ANY);
        FileUtils.setPath(getAlbumStorageDir("smartpolicing").getAbsolutePath());
        WidgetUtils.UpdateWidget(this.getApplicationContext());
        ApiClient.setAppContext(this.getApplicationContext());
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), albumName);
        if (!file.exists() && !file.mkdirs()) {
            Log.e(TAG, "Directory '" + albumName + "' not created");
        }
        return file;
    }

    public ObjectGraph objectGraph() {
        return objectGraph;
    }

    @Provides @Singleton
    public Bus providesBus() {
        return mBus;
    }
}

