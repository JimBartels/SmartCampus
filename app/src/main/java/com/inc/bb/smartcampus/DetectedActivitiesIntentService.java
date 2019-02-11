package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class DetectedActivitiesIntentService  extends IntentService {
    public static volatile boolean shouldContinue = true;

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
        if(!shouldContinue){stopSelf();}
        Log.d(TAG, "onHandleIntent:");
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Long ActivityTimeStamp = result.getTime();

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        int maxConf = Integer.MIN_VALUE;
        int maxConfType=0;

        for (DetectedActivity activity : detectedActivities) {
            if(activity.getConfidence() > maxConf){
                maxConf = activity.getConfidence();
                maxConfType = activity.getType();
            }
            /*activity.g
            Log.i(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            broadcastActivity(activity);*/
        }
        intent.setAction(ConstantsClassifier.ACTIVITY_BROADCAST_ACTION);
        Log.d(TAG, "onHandleIntent: nearbroadcast");
        intent.putExtra("confidence", maxConf);
        intent.putExtra("type", maxConfType);
        intent.putExtra("timestamp", ActivityTimeStamp);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }
}
