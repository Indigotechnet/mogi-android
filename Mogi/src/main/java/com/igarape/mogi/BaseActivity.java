package com.igarape.mogi;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.igarape.mogi.MogiApp;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by felipeamorim on 08/07/2013.
 */
public abstract class BaseActivity extends FragmentActivity {

    @Inject
    protected Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MogiApp)getApplication()).objectGraph().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }
}
