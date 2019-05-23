package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GoogleFusedLocations extends IntentService {
    Handler mHandler;
    String TAG = "GoogleFusedLocations";
    public static volatile boolean shouldContinue = true;
    public volatile static ResolvableApiException rae;
    boolean UserActivityNotYetRunning = true;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = database.getReference();

    @Override
    public void onCreate() {
        mHandler = new Handler();
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    //Location request settings constants
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private final static long UPDATE_INVTERVAL_IN_MILLISECONDS = 500;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1;
    static String userid;
    //Googleapi and location clients
    private FusedLocationProviderClient mFusedLocationClient;
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;

    public GoogleFusedLocations() {
        super("GoogleFusedLocations");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Assignment of global variables for google location client after starting it in the GPS
        // thread. Also connects to the ActivityRecognition api needed for Activity detection.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        Log.d(TAG, "onHandleIntent: ");

        //Creates settings to be passed to google's fusion location client and sets callback
        //This is the core of the app
        createLocationRequest();
        buildLocationSettingsRequest();
        createLocationCallback();
        startLocationUpdates();
    }

    // Constructor of locationrequest, sets certain settings of the locationrequest (interval,
    // priority etc) for the client that sends the location updates.
    private void createLocationRequest() {
        Log.d(TAG, "createLocationRequest: ");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INVTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    //Builds the location request upon the settings build in createLocationRequest
    private void buildLocationSettingsRequest() {
        Log.d(TAG, "buildLocationSettingsRequest: ");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(this.mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    //This is called whenever a new location is found,
    // this is the clock where updatelocationUI runs on.
    private void createLocationCallback() {
        Log.d(TAG, "createLocationCallback: ");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Long timestamp = System.currentTimeMillis();
                if (!shouldContinue) {
                    broadcastStop();
                    stopSelf();
                }
                broadcastLocation(timestamp, locationResult);

            }
        };
    }

    private void broadcastStop() {
        Intent intent = new Intent();
        intent.setAction("GoogleFusedLocations.SEND_NEW_LOCATION");
        Log.d(TAG, "broadcastingstop");
        intent.putExtra("shouldContinue",false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void broadcastLocation(Long timestamp, LocationResult locationResult) {
        mDatabase.child("users").setValue(userid);
        mDatabase.child("users").child(userid).child("latitude").setValue(locationResult.getLastLocation().getLatitude());
        mDatabase.child("users").child(userid).child("longitude").setValue(locationResult.getLastLocation().getLongitude());

        Log.d("bla bla bla", String.valueOf(locationResult.getLastLocation().getLatitude()));

        Intent intent = new Intent();
        intent.setAction("GoogleFusedLocations.SEND_NEW_LOCATION");
        Log.d(TAG, "broadcastingLocation");
        intent.putExtra("error", false);
        intent.putExtra("longitude", locationResult.getLastLocation().getLongitude());
        intent.putExtra("latitude", locationResult.getLastLocation().getLatitude());
        intent.putExtra("accuracy", locationResult.getLastLocation().getAccuracy());
        intent.putExtra("speed", locationResult.getLastLocation().getSpeed());
        intent.putExtra("heading", locationResult.getLastLocation().getBearing());
        intent.putExtra("timeStamp", timestamp);
        intent.putExtra("shouldContinue",true);
        intent.putExtra("uuid",UUID.randomUUID().toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // Starts the actual location updates from the settings that have been built before. Sets on
    // success listeners and on failure listeners to inform whether the locationupdates request was
    // successful.
    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: ");
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
                        Log.d(TAG, "onSuccess:");
                        //updateLocationUI(System.currentTimeMillis());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            rae = (ResolvableApiException) e;
                            Intent intent = new Intent();
                            intent.setAction("GoogleFusedLocations.RESOLUTION_REQUIRED");
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            Log.e(TAG, "onFailure: " + e.toString());
                break;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        super.onDestroy();
    }

    // Starts the tracking of UserActivity via BackgroundDetectedAcitiviesService, which sends
    // activities list to DetectedActivitiesIntentService, this gets the highest confidense activity
    // type and broadcasts to setBroadcastReceiver
    private void startTrackingUserActivity(){
        Intent intent = new Intent(getApplicationContext(), BackgroundDetectedActivitiesService.class);
        Log.d(TAG, "startTrackingUserActivity: ");
        getApplicationContext().startService(intent);
    }

}
