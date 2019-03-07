package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HuaweiCommunications extends IntentService implements okHttpPost.AsyncResponse {

    //GPS location global variables
    double Longitude;
    double Latitude;
    float Heading= Float.valueOf(0);
    float Accuracy= Float.valueOf(0);
    float Speed = Float.valueOf(0);

    //Huawei communication
    String huaweiUrl = "http://217.110.131.79:2020/mobile/dataapp";

    //Message types for logging (what kind of log is needed)
    private final static int LOGGING_NOTNEEDED = 0;
    private final static int LOGGING_GPS = 1;
    private final static int LOGGING_STATUS = 2;
    private final static int LOGGING_VEHICLE = 3;
    private final static int LOGGING_HUAWEI_SENT = 4;
    private final static int LOGGING_HUAWEI_RECEIVED = 5;
    private final static int LOGGING_TAXI_SENT = 6;
    private final static int LOGGING_TAXI_RECEIVED = 7;

    String username;
    String TAG = "HuaweiCommunications";
    BroadcastReceiver locationsBroadcastReceiver;

    //Logging layout check variables
    BroadcastReceiver layoutResponseBroadcastReceiver;
    boolean isLoggingSwitched = false;
    String experimentNumber;
    String runNumber;

    public HuaweiCommunications() {
        super("HuaweiCommunications");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        username = intent.getStringExtra("username");
        createBroadcastReceiverLayoutResponse();
        createBroadcastReceiverLocations();
    }

    private void createBroadcastReceiverLayoutResponse() {
        layoutResponseBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: layout");
                isLoggingSwitched = intent.getBooleanExtra("loggingEnabled",
                        false);
                Log.d(TAG, "onReceive: " + isLoggingSwitched);
                if (isLoggingSwitched) {
                    experimentNumber = intent.getStringExtra("experimentNumber");
                    runNumber = intent.getStringExtra("runNumber");
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GpsActivity.LAYOUT_RESPONSE");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                layoutResponseBroadcastReceiver, intentFilter);
    }

    private void broadcastIsLoggingEnabled(){
        Intent intent = new Intent();
        intent.setAction("OneM2MForwardCommunications.LAYOUT_CHECK");
        Log.d(TAG, "checking if logging enabled");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void createBroadcastReceiverLocations() {
        locationsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                broadcastIsLoggingEnabled();
                Longitude = intent.getDoubleExtra("longitude",'0');
                Latitude = intent.getDoubleExtra("latitude",'0');
                Accuracy = intent.getFloatExtra("accuracy",'0');
                Heading = intent.getFloatExtra("heading",'0');
                Speed = intent.getFloatExtra("speed",'0');
                Long timeStamp = intent.getLongExtra("timeStamp",0);
                String uuid = intent.getStringExtra("uuid");
                publishGpsData(Latitude,Longitude,Accuracy,timeStamp,String.valueOf(Speed),String.valueOf(Heading),uuid);
                Log.d(TAG, "onReceive: ");
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GoogleFusedLocations.SEND_NEW_LOCATION");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationsBroadcastReceiver, intentFilter);
    }

    // Function for creating the final json to be sent to the publishandlogmessage function for
    // sending and logging of the GPS information to OneM2M. The inputs concern the found lat,lon
    // speed etc from last found location of the Google mfusedlocationclient.
    private void publishGpsData(Double latitude, Double longitude, Float Accuracy,
                                Long formattedDate, String speedGPS, String manualBearing, String uuid) {
        String formattedDateString = "UTC" + Long.toString(formattedDate);
        String conHuawei = "{\"type\":5,\"id\":" + username + ",\"timestampUtc\":" +
                formattedDateString + ",\"lon\":" + longitude + ",\"lat\":"+ latitude +
                ",\"speed\":"+ speedGPS + ",\"heading\":"+manualBearing+ ",\"accuracy\":"+
                Accuracy+ ",\"UUID\": " + "\"" + uuid + "\"" + "}";
        okHTTPPost(huaweiUrl,conHuawei);
        if(isLoggingSwitched){
            Intent logIntent = new Intent();
            logIntent.setAction("OneM2M.ForwardLogging");
            logIntent.putExtra("messageType",LOGGING_HUAWEI_SENT);
            logIntent.putExtra("logmsg",conHuawei);
            logIntent.putExtra("uuid",uuid);
            logIntent.putExtra("generationTimeStamp",formattedDate);
            logIntent.putExtra("username",username);
            logIntent.putExtra("runNumber",runNumber);
            logIntent.putExtra("experimentNumber",experimentNumber);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(logIntent);
        }
    }

    // Function called when asynctasks are finished. In this case handles the response received by
    // the OkHTTP Huawei connection asynctask. Whenever Huawei sends something back (whenever we
    // send them something by post) this function is called. This then handles the rectangle
    // placement and speed indication in the google maps layout and also handles logging of Huawei
    // messaging as well as notifications for the rectangle.
    @Override
    public void processFinish(Bundle output) {
        broadcastIsLoggingEnabled();
        if(output!=null && output.getString("error")==null) {
            Log.d(TAG, "processFinish: " + output.getString("returnMessage")
                    + "," + output.getBoolean("isInRectangle"));
            if(isLoggingSwitched){
                Intent logIntent = new Intent();
                logIntent.setAction("OneM2M.ForwardLogging");
                logIntent.putExtra("messageType",LOGGING_HUAWEI_RECEIVED);
                logIntent.putExtra("logmsg",output.
                        getString("returnMessage"));
                logIntent.putExtra("username",username);
                logIntent.putExtra("runNumber",runNumber);
                logIntent.putExtra("experimentNumber",experimentNumber);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(logIntent);
            }
            double[] rectangleLat = output.getDoubleArray("rectangleLat");
            double[] rectangleLon = output.getDoubleArray("rectangleLon");
            Intent UIintent = new Intent();
            UIintent.putExtra("rectangleLat",rectangleLat);
            UIintent.putExtra("rectangleLon",rectangleLon);
            UIintent.putExtra("isInRectangle",output.getBoolean("isInRectangle"));
            UIintent.setAction("HuaweiCommunications.CAR_DATA");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(UIintent);
        }
        if(output!=null && output.getString("error")!=null) {
            Log.d(TAG, "processFinish: " + output.getString("error"));
        }
    }

    // Executes an OkHTTPpost asynctask to send a json to a certain URL that is indicated by the url
    // and json. results of this is handled in process finish (used for Huawei communication).
    void okHTTPPost(String url, String json) {
        Log.d(TAG, "okHTTPPost: " + url + ", " + json);
        okHttpPost okHttpPost = new okHttpPost(this);
        String[] string = new String[3];
        string[0]=url;
        string[1]=json;
        string[2]=username;
        okHttpPost.execute(string);
    }
}
