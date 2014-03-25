package com.igarape.mogi;

import android.content.Context;

import com.igarape.mogi.location.LocationService;
import com.igarape.mogi.manager.MainActivity;
import com.igarape.mogi.recording.RecordingService;
import com.igarape.mogi.recording.StreamingService;
import com.igarape.mogi.server.ConnectivityStatusReceiver;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                MainActivity.class,
                LocationService.class,
                ConnectivityStatusReceiver.class,
                RecordingService.class,
                StreamingService.class,
        }
)
public class MogiModule {
    private final Context appContext;
    // private final RestAdapter restAdapter;

    public MogiModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @Singleton
    Bus providesBus() {
        return new Bus(ThreadEnforcer.MAIN);
    }

    @Provides
    @Singleton
    Context providesContext() {
        return appContext;
    }
}
