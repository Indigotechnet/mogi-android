package com.igarape.mogi.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.igarape.mogi.utils.FileUtils;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * Created by felipeamorim on 09/09/2013.
 */
public class UploadService extends Service {
    public static String TAG = "UploadService";
    private final GenericExtFilter filter = new GenericExtFilter(".mp4");
    private ArrayList<File> videos = new ArrayList<File>();

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if ( videos == null || videos.size() == 0 ) {
            File dir = new File(FileUtils.getPath());
            videos = new ArrayList<File>(Arrays.asList(dir.listFiles(filter)));
        }

        uploadLocations();
        if (VideoUtils.isRecordVideos()){
            uploadVideos();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadLocations() {
        InputStream is;
        BufferedReader br;
        String line;
        String[] values;

        try {
            is = new FileInputStream(FileUtils.getLocationsFilePath());
            br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            return;
        }

        JSONArray locations = new JSONArray();
        JSONObject curLoc;
        try {
            while((line = br.readLine()) != null) {
                values = line.split(";");
                curLoc = new JSONObject();
                curLoc.put("lat", values[0]);
                curLoc.put("lng", values[1]);
                curLoc.put("date", values[4]);

                locations.put(curLoc);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        br = null;
        is = null;

        ApiClient.post("/locations", locations, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                FileWriter out = null;
                try {
                    out = new FileWriter(FileUtils.getLocationsFilePath(),true);
                    out.write("");
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void uploadVideos() {

        if ( videos.size() == 0 ) {
            stopSelf();
            return;
        }

        final File nextVideo = videos.remove(0);

        if ( nextVideo != null && nextVideo.exists()) {
            RequestParams params = new RequestParams();
            try {
                params.put("video", nextVideo);

            } catch (FileNotFoundException e) {
                // Video not found, try next one
                uploadVideos();
                return;
            }

            params.put("date", TimeZone.getDefault().getDisplayName());

            ApiClient.post("/videos", params, new TextHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                Log.i(TAG, "video uploaded: "+ nextVideo.getName());
                    uploadVideos();
                }

                @Override
                public void onFailure(String responseBody, Throwable error) {
                    Log.e(TAG, "video not uploaded: "+ nextVideo.getName(), error);
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
