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
import android.view.inputmethod.InputMethodManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.igarape.mogi.MogiApp;
import com.igarape.mogi.R;
import com.igarape.mogi.manager.MainActivity;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.Identification;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AuthenticationActivity extends Activity {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static String TAG = AuthenticationActivity.class.getName();
    GoogleCloudMessaging gcm;
    Context context;
    String regid = null;

    RequestQueue queue;

    EditText txtId;
    EditText txtPwd;
    ProgressDialog pDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Identification.setAccessToken(this, null);
        StateMachine.getInstance().startServices(State.NOT_LOGGED, getBaseContext(), null);

        context = getApplicationContext();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        }

        txtId = (EditText) findViewById(R.id.txtLoginUser);
        txtId.setText(Identification.getUserLogin(this));
        
        /**
         * Appears a hack
         * On login_activity I added
         * android:focusable="true"
         * android:focusableInTouchMode="true"
         */
        txtId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtId.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager keyboard = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            keyboard.showSoftInput(txtId, 0);
                        }
                    },200);
                }
            }
        });

        txtPwd = (EditText) findViewById(R.id.txtLoginPassword);

        queue = Volley.newRequestQueue(this);
        ((Button) findViewById(R.id.btn_login_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeLoginRequest();
            }
        });

        ((Button) findViewById(R.id.btn_login_cancel)).setOnClickListener(new View.OnClickListener() {
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

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences("AUTH", Context.MODE_PRIVATE);
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
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

        ApiClient.post("/token", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(String responseBody, Throwable error) {
                if (pDialog != null) {
                    pDialog.dismiss();
                    pDialog = null;
                }
                Log.e(TAG, "Error: " + responseBody, error);
                Toast.makeText(AuthenticationActivity.this, "Unable to login!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable e) {
                if (pDialog != null) {
                    pDialog.dismiss();
                    pDialog = null;
                }
                if (statusCode == 401) {
                    Toast.makeText(AuthenticationActivity.this, AuthenticationActivity.this.getString(R.string.unauthorized_login), Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "Error: " + responseBody, e);
                    Toast.makeText(AuthenticationActivity.this, AuthenticationActivity.this.getString(R.string.no_server_login), Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onSuccess(JSONObject response) {
                successResponse(response);
            }

            private void successResponse(JSONObject response) {
                String token = null;
                try {
                    token = (String) response.get("token");
                    String ipAddress = (String) response.get("ipAddress");
                    if (ipAddress != null) {
                        Identification.setServerIpAddress(ipAddress);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "error on login", e);
                }
                if (pDialog != null) {
                    pDialog.dismiss();
                    pDialog = null;
                }
                try {
                    Identification.setStreamingPort(Integer.parseInt((String) response.get("streamingPort")));
                    Identification.setStreamingUser((String) response.get("streamingUser"));
                    Identification.setStreamingPassword((String) response.get("streamingPassword"));
                    Identification.setStreamingPath((String) response.get("streamingPath"));
                    Identification.setUserName((String) response.get("userName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Identification.setAccessToken(getBaseContext(), token);
                Identification.setUserLogin(getBaseContext(), txtId.getText().toString());
                ApiClient.setToken(token);
                Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                startActivity(intent);
                AuthenticationActivity.this.finish();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                try {
                    successResponse(new JSONObject(responseBody));
                } catch (JSONException e) {
                    Log.e(TAG, "error on login", e);
                }

            }
        });
    }
}
