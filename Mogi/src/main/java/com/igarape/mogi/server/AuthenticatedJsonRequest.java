package com.igarape.mogi.server;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by felipeamorim on 27/08/2013.
 */
public class AuthenticatedJsonRequest extends JsonObjectRequest {
    private final String accessToken;

    public AuthenticatedJsonRequest(String token, int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        accessToken = token;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<String,String>();
        params.put("Authorization", accessToken);
        return params;
    }
}
