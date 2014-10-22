package com.igarape.mogi.utils;

import com.igarape.mogi.server.ApiClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

/**
 * Created by brunosiqueira on 26/02/2014.
 */
public class VideoUtils {
    public static final int DEGREES = 90;
    public static final int MAX_DURATION_MS = 600000;
    public static final long MAX_SIZE_BYTES = 15000000;

    private static boolean recordVideos = true;

    public static boolean isRecordVideos() {
        return recordVideos;
    }

    public static void setRecordVideos(boolean recordVideos) {
        VideoUtils.recordVideos = recordVideos;
    }

    public static void sentVideos(RequestParams params, TextHttpResponseHandler responseHandler) {
        ApiClient.post("/videos", params, responseHandler);
    }

    public static void sentVideos(String login, RequestParams params, TextHttpResponseHandler responseHandler) {
        ApiClient.post("/videos/" + login, params, responseHandler);
    }
}
