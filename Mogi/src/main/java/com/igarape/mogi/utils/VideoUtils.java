package com.igarape.mogi.utils;

/**
 * Created by brunosiqueira on 26/02/2014.
 */
public class VideoUtils {
    private static boolean recordVideos = false;

    public static boolean isRecordVideos() {
        return recordVideos;
    }

    public static void setRecordVideos(boolean recordVideos) {
        VideoUtils.recordVideos = recordVideos;
    }
}
