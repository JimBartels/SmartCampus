package com.inc.bb.smartcampus;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

;

public class GpsActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String TAG;
    GoogleApiClient client;
    private FusedLocationProviderClient mFusedLocationClient;
    private TextView viewLatitude;
    private TextView viewLongitude;
    private TextView viewLocation;
    private Location mCurrentlocation;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;
    private String mLastUpdateTime;
    public  String longitude;
    public  String latitude;
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private String studentnumber;
    private final static long UPDATE_INVTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 2500;
    private String userId;
    private String intentString;
    private LocationRequest mLocationRequest;
    private Drawable drawable1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        FirebaseUser user = mAuth.getCurrentUser();
        userCheck(user);
        getUsername(user);
        drawable1 = ContextCompat.getDrawable(getApplicationContext(),R.drawable.snackbarshape);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        viewLatitude = (TextView) findViewById(R.id.latitude);
        viewLongitude = (TextView) findViewById(R.id.longitude);
        viewLocation = (TextView) findViewById(R.id.location);
        updateValuesFromBundle(savedInstanceState);
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient=LocationServices.getSettingsClient(this);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        Boolean a = checkPermissions();

        if(a==false){
            requestPermission();
        }
        if(a==true){;

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermissions()){
            startLocationUpdates();
        }
        else if(!checkPermissions()){
            requestPermission();
        }
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                        updateLocationUI();

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try{
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(GpsActivity.this,REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie){

                                }
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed. Please fix in settings";
                                Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();;
                        }}
                });
    }

    private void requestPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale){
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "We need your gps data for essential app functions and research",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  ActivityCompat.requestPermissions(GpsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                          REQUEST_PERMISSIONS_REQUEST_CODE);

            }});
            int snackbarTextId= android.support.design.R.id.snackbar_text;
            TextView textView =(TextView) snackbar.getView().findViewById(snackbarTextId);
            textView.setTextColor(Color.WHITE);
            snackbar.getView().setBackground(drawable1);
            snackbar.show();
        }
        else{
            ActivityCompat.requestPermissions(GpsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE){
            if(grantResults.length <= 0){
                //TODO als de request interuppted is, hoeft in principe niks mee te gebeuren
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "You have connected to the GPS database, data is used for research and functionality",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }});
                int snackbarTextId= android.support.design.R.id.snackbar_text;
                TextView textView =(TextView) snackbar.getView().findViewById(snackbarTextId);
                textView.setTextColor(Color.WHITE);
                snackbar.getView().setBackground(drawable1);
                snackbar.show();
                startLocationUpdates();
            }else{
            }
        }
    }

    private boolean checkPermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;

    }

    private void userCheck(FirebaseUser user) {
        if (user == null) {
            Intent loginIntent = new Intent(GpsActivity.this, MainActivity.class);
            startActivity(loginIntent);
        }
    }

    private void getUsername(FirebaseUser user) {
        intentString = getIntent().getStringExtra("userId");
        if(intentString!=null){
            userId=intentString;
        }else{
            userId= user.getEmail();
            userId = userId.replace("@Random.com", "");
        }

    }


    private void createLocationCallback() {
        mLocationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentlocation= locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();

            }
        };

    }

    private void updateLocationUI() {
        if(mCurrentlocation!=null){
            userId = userId.replace("@random.com", "");
            latitude = String.format(Locale.ENGLISH,"%f", mCurrentlocation.getLatitude());
            longitude = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getLongitude());
            viewLatitude.setText(latitude);
            viewLongitude.setText(longitude);
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = df.format(c.getTime());
            formattedDate.trim();
            mDatabase.child("users").child(userId).child("lastTimeUpdate").setValue(formattedDate);
            mDatabase.child("users").child(userId).child("longitude").setValue(longitude);
            mDatabase.child("users").child(userId).child("latitude").setValue(latitude);
            Double Longitude = mCurrentlocation.getLongitude();
            Double Latitude = mCurrentlocation.getLatitude();
            Double bound1la = 51.445110;
            Double bound2la= 51.452770;
            Double bound1lo = 5.500690;
            Double bound2lo = 5.481070;
            if(Latitude>bound1la && Latitude<bound2la && Longitude<bound1lo && Longitude>bound2lo){

                viewLocation.setText("You are currently on Tu/e campus");
            mDatabase.child("users").child(userId).child("onTue").setValue("yes");}
            else{mDatabase.child("users").child(userId).child("onTue").setValue("no");
            viewLocation.setText("You are currently not on Tu/e campus");}
        }


    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentlocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "destroy");
        super.onDestroy();
    }
    private void createLocationRequest(){
        mLocationRequest= new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INVTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

    }

    private void buildLocationSettingsRequest(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest= builder.build();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {

    }
}
