package com.igarape.mogi.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.igarape.mogi.MogiApp;
import com.igarape.mogi.R;
import com.igarape.mogi.location.LocationService;
import com.igarape.mogi.manager.MainActivity;
import com.igarape.mogi.utils.Identification;
import com.igarape.mogi.utils.WidgetUtils;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthenticationActivity extends Activity {
    public static String TAG = AuthenticationActivity.class.getName();

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid = null;

    RequestQueue queue;

    EditText txtId;
    EditText txtPwd;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        context = getApplicationContext();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        }

        txtId = (EditText)findViewById(R.id.txtLoginUser);
        txtId.setText(Identification.getUserLogin(this));

        txtPwd = (EditText)findViewById(R.id.txtLoginPassword);

        queue = Volley.newRequestQueue(this);
        ((Button)findViewById(R.id.btn_login_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeLoginRequest();
            }
        });

        ((Button)findViewById(R.id.btn_login_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
        queue.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.authentication, menu);
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences("AUTH", Context.MODE_PRIVATE);
    }

    private void registerInBackground() {
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(MogiApp.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);

                if (Identification.getAccessToken(context) != null ) {
                    startActivity(new Intent(context, MainActivity.class));
                }
            }
        }.execute(null, null, null);
    }

    private void makeLoginRequest() {
        RequestParams params = new RequestParams();
        params.put("username", txtId.getText().toString());
        params.put("password", txtPwd.getText().toString());
        params.put("scope", "client");
        params.put("gcm_registration", regid);

        pDialog = ProgressDialog.show(this, "Fazendo login", "Por favor aguarde...", true);
//TODO substituir por JsonResponseHandler - pegando o ip do servidor tb - Identification.java
        ApiClient.post("/token", params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(String responseBody, Throwable error) {
                pDialog.hide();
                pDialog = null;
                Log.e(TAG, "Error: " + responseBody, error);
                Toast.makeText(AuthenticationActivity.this, "Unable to login!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                pDialog.hide();
                pDialog = null;
                WidgetUtils.UpdateWidget(AuthenticationActivity.this.getApplicationContext());
                Identification.setAccessToken(getBaseContext(), responseBody);
                Identification.setUserLogin(getBaseContext(), txtId.getText().toString());
                ApiClient.setToken(responseBody);
                startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));

            }
        });
    }
}
