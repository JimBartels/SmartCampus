package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognizedService extends IntentService {
    private static final String TAG = "ActivityRecognizedService";

    public ActivityRecognizedService(){
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name){
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)){
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivity(result.getProbableActivities());
        }
    }

    private void handleDetectedActivity(List<DetectedActivity> probableActivities){
        for (DetectedActivity activity: probableActivities){
            switch(activity.getType()){
                case DetectedActivity.IN_VEHICLE: {
                    Log.d(TAG, "handleDetectedActivity: IN_VERHICLE: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_BICYCLE:{
                    Log.d(TAG, "handleDetectedActivity: ON_BYCYCLE: " + activity.getConfidence());
                    break;
                }
                case  DetectedActivity.ON_FOOT:{
                    Log.d(TAG, "handleDetectedActivity: ON_FOOT: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING:{
                    Log.d(TAG, "handleDetectedActivity: RUNNING: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL:{
                    Log.d(TAG, "handleDetectedActivity: STILL: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING:{
                    Log.d(TAG, "handleDetectedActivity: WALKING: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.TILTING:{
                    Log.d(TAG, "handleDetectedActivity: TILTING: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.UNKNOWN:{
                    Log.d(TAG, "handleDetectedActivity: UNKNOWN: " + activity.getConfidence());
                    break;
                }
            }
        }
    }
}
