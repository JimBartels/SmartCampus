package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class DetectedActivitiesIntentService  extends IntentService {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Long ActivityTimeStamp = result.getTime();

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        int maxConf = Integer.MIN_VALUE;
        DetectedActivity maxConfActivity = null;

        for (DetectedActivity activity : detectedActivities) {
            if(activity.getConfidence() > maxConf){
                maxConf = activity.getConfidence();
                maxConfActivity = activity;
            }
            /*activity.g
            Log.i(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            broadcastActivity(activity);*/
        }
        broadcastActivity(maxConfActivity, ActivityTimeStamp);
    }

    private void broadcastActivity(DetectedActivity activity, long activitytimestamp) {
        Intent intent = new Intent(ConstantsClassifier.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("timestamp", activitytimestamp);
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
