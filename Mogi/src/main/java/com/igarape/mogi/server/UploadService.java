package com.igarape.mogi.server;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import com.igarape.mogi.BaseService;
import com.igarape.mogi.R;
import com.igarape.mogi.states.State;
import com.igarape.mogi.states.StateMachine;
import com.igarape.mogi.utils.FileUtils;
import com.igarape.mogi.utils.HistoryUtils;
import com.igarape.mogi.utils.LocationUtils;
import com.igarape.mogi.utils.UploadProgressUtil;
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
public class UploadService extends BaseService {
    public static String TAG = UploadService.class.getName();
    public static boolean isUploading = false;
    private static int ServiceID = 4;
    private final GenericExtFilter filter = new GenericExtFilter(".mp4");
    private ArrayList<File> videos = new ArrayList<File>();
    private int totalVideos = 0;
    private int completedVideos = 0;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notification_upload_title))
                .setContentText(getString(R.string.notification_upload_description))
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(ServiceID, notification);

        if (isUploading) {
            return START_STICKY;
        }

        uploadAllFiles(FileUtils.getPath(), FileUtils.getLocationsFilePath(), FileUtils.getHistoriesFilePath(), null);

        String[] users = FileUtils.getUserFolders();
        for (String userLogin : users){
            uploadAllFiles(FileUtils.getPath(userLogin), FileUtils.getLocationsFilePath(userLogin), FileUtils.getHistoriesFilePath(userLogin), userLogin);
        }

        return super.onStartCommand(intent, flags, startId);
    }


    protected void uploadAllFiles(String path, String locationsFilePath, String historiesFilePath, String userLogin) {
        if (videos == null || videos.size() == 0) {

            File dir = new File(path);
            File[] files = dir.listFiles(filter);
            if (files != null && files.length > 0) {
                videos = new ArrayList<File>(Arrays.asList(files));
            } else {
                videos = new ArrayList<File>();
            }
        }

        uploadLocations(locationsFilePath);
        uploadHistory(historiesFilePath);
        if (VideoUtils.isRecordVideos()) {
            totalVideos = videos.size();
            completedVideos = 0;
            UploadProgressUtil.sendUpdate(getApplicationContext(), totalVideos, completedVideos);
            uploadVideos(userLogin);
        }
    }

    private void uploadHistory(final String historiesFilePath) {
        InputStream is;
        BufferedReader br;
        String line;
        String[] values;
        File file = new File(historiesFilePath);
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

        JSONArray histories = new JSONArray();
        try {
            while ((line = br.readLine()) != null) {
                JSONObject json = new JSONObject(line);
                if (json.getString("previousState") != null) {
                    histories.put(json);
                }
            }

            HistoryUtils.sendHistories(histories, new JsonHttpResponseHandler() {
                private void deleteFile() {
                    File out = new File(historiesFilePath);
                    out.delete();
                    Log.i(TAG, "histories sent successfully");
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
                    Log.e(TAG, "histories not sent successfully");
                }

                @Override
                public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                    Log.e(TAG, "histories not sent successfully", e);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    Log.e(TAG, "histories not sent successfully", e);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                    Log.e(TAG, "histories not sent successfully", e);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "error reading histories file", e);
        }
        catch (JSONException e) {
            Log.e(TAG, "error reading histories file", e);
        } finally {
            try {
                if (br != null){br.close();}
            } catch (IOException e) {

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isUploading = false;
    }

    private void uploadLocations(final String locationsFilePath) {
        InputStream is;
        BufferedReader br;
        String line;
        String[] values;
        File file = new File(locationsFilePath);
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
        try {
            while ((line = br.readLine()) != null) {
                JSONObject json = new JSONObject(line);
                if (json.getString("lat") != null && json.getString("lat").length() > 0) {
                    locations.put(json);
                }
            }

            LocationUtils.sendLocations(locations, new JsonHttpResponseHandler() {
                private void deleteFile() {
                    File out = new File(locationsFilePath);
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
        } catch (IOException e) {
            Log.e(TAG, "error reading location file", e);
        }
        catch (JSONException e) {
            Log.e(TAG, "error reading location file", e);
        } finally {
            try {
                if (br != null){br.close();}
            } catch (IOException e) {

            }
        }
    }


    private void uploadVideos(final String userLogin) {

        ConnectivityManager systemService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (videos.size() == 0 || !StateMachine.getInstance().isInState(State.UPLOADING)) {
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
                uploadVideos(userLogin);
                return;
            }
            DateFormat df = new SimpleDateFormat(FileUtils.DATE_FORMAT);
            params.put("date", df.format(new Date(nextVideo.lastModified())));

            TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                    Log.i(TAG, "video uploaded: " + nextVideo.getName());
                    nextVideo.delete();
                    completedVideos++;
                    UploadProgressUtil.sendUpdate(getApplicationContext(), totalVideos, completedVideos);
                    uploadVideos(userLogin);
                }

                @Override
                public void onFailure(String responseBody, Throwable error) {
                    Log.e(TAG, "video not uploaded: " + nextVideo.getName() + " - " + responseBody, error);
                    uploadVideos(userLogin);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(TAG, "video not uploaded: " + nextVideo.getName(), error);
                    uploadVideos(userLogin);
                }
            };
            if (userLogin == null) {
                VideoUtils.sentVideos(params, responseHandler);
            } else {
                VideoUtils.sentVideos(userLogin, params, responseHandler);
            }
        } else {
            stopSelf();
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
