package com.igarape.mogi.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.LocationUtils;
import com.igarape.mogi.utils.VideoUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by felipeamorim on 09/09/2013.
 */
public class UploadService extends Service {
    public static String TAG = UploadService.class.getName();
    private final GenericExtFilter filter = new GenericExtFilter(".mp4");
    private ArrayList<File> videos = new ArrayList<File>();
    public static boolean isUploading = false;
    private Intent intent;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        if (isUploading) {
            return START_STICKY;
        }
        this.intent = intent;
        if (videos == null || videos.size() == 0) {
            File dir = new File(FileUtils.getPath());
            videos = new ArrayList<File>(Arrays.asList(dir.listFiles(filter)));
        }

        uploadLocations();
        if (VideoUtils.isRecordVideos()) {
            uploadVideos();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isUploading = false;
    }

    private void uploadLocations() {
        InputStream is;
        BufferedReader br;
        String line;
        String[] values;
        File file = new File(FileUtils.getLocationsFilePath());
        if (!file.exists()) {
            if (videos.isEmpty()) {
                isUploading = false;
            }
            return;
        }
        try {
            is = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            return;
        }

        JSONArray locations = new JSONArray();
        JSONObject curLoc;
        try {
            while ((line = br.readLine()) != null) {
                values = line.split(";");
                if (!TextUtils.isEmpty(values[0]) && !TextUtils.isEmpty(values[2]) && !TextUtils.isEmpty(values[4])) {
                    locations.put(LocationUtils.buildJson(values[0], values[1], values[4]));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        br = null;
        is = null;


        LocationUtils.sendLocations(locations, new JsonHttpResponseHandler() {
            private void deleteFile() {
                File out = new File(FileUtils.getLocationsFilePath());
                out.delete();
                Log.i(TAG, "location sent successfully");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                deleteFile();
            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {//
                deleteFile();
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "location not sent successfully");
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "location not sent successfully", e);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                Log.e(TAG, "location not sent successfully", e);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                Log.e(TAG, "location not sent successfully", e);
            }
        });
    }


    private void uploadVideos() {

        ConnectivityManager systemService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (videos.size() == 0 || FileUtils.canUpload(systemService.getActiveNetworkInfo(), this.intent)) {
            isUploading = false;
            stopSelf();
            return;
        }

        final File nextVideo = videos.remove(0);

        if (nextVideo != null && nextVideo.exists()) {
            RequestParams params = new RequestParams();
            try {
                params.put("video", nextVideo);

            } catch (FileNotFoundException e) {
                // Video not found, try next one
                uploadVideos();
                return;
            }
            DateFormat df = new SimpleDateFormat(FileUtils.DATE_FORMAT);
            params.put("date", df.format(new Date(nextVideo.lastModified())));

            ApiClient.post("/videos", params, new TextHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                    Log.i(TAG, "video uploaded: " + nextVideo.getName());
                    nextVideo.delete();
                    uploadVideos();
                }

                @Override
                public void onFailure(String responseBody, Throwable error) {
                    Log.e(TAG, "video not uploaded: " + nextVideo.getName(), error);
                    stopSelf();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(TAG, "video not uploaded: " + nextVideo.getName(), error);
                    stopSelf();
                }
            });
        }
    }

    public class GenericExtFilter implements FilenameFilter {

        private String ext;

        public GenericExtFilter(String ext) {
            this.ext = ext;
        }

        public boolean accept(File dir, String name) {
            return (name.endsWith(ext));
        }
    }

    // Loop video folder, upload everything
}
