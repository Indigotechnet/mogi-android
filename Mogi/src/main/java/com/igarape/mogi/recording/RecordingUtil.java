package com.igarape.mogi.recording;

/**
 * Created by brunosiqueira on 18/02/2014.
 */
public class RecordingUtil {
    public static boolean isInAction() {
        return StreamingService.IsStreaming || RecordingService.IsRecording;
    }
}
