package com.inc.bb.smartcampus;

import android.Manifest;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hypertrack.hyperlog.HyperLog;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class GpsActivity extends AppCompatActivity implements MapViewConstants, okHttpPost.AsyncResponse, OnMapReadyCallback {

    GoogleApiClient mGoogleApiClient;

    //Maps
    private GoogleMap mMap;

    //buildings
    private GroundOverlay fluxOverlay;
    private static final LatLng FLUX = new LatLng(51.447425, 5.491944);
    private Marker Flux;
    private GroundOverlay metaforumOverlay;
    private static final LatLng METAFORUM = new LatLng(51.447398, 5.487480);
    private Marker Metaforum;
    private GroundOverlay atlasOverlay;
    private static final LatLng ATLAS = new LatLng(51.447692, 5.486107);
    private Marker Altas;
    private GroundOverlay auditoriumOverlay;
    private static final LatLng AUDITORIUM = new LatLng(51.447625, 5.484348);
    private Marker Auditorium;
    private GroundOverlay vertigoOverlay;
    private static final LatLng VERTIGO = new LatLng(51.445967, 5.484991);
    private Marker Vertigo;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String TAG = "GpsActivity";
    private FusedLocationProviderClient mFusedLocationClient;

    //Layout elements
    private TextView viewLatitude;
    private TextView viewLongitude;
    private TextView viewLocation;
    private TextView viewBearing;
    private TextView viewSpeed;
    private Switch gpsHolder;

    private Location mCurrentlocation;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;
    public  String longitude;
    public  String latitude;
    public  String bearing;
    public  String bearingAccuracy;
    public String speed;
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private final static long UPDATE_INVTERVAL_IN_MILLISECONDS = 500;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1;
    private LocationRequest mLocationRequest;
    private Drawable drawable1;
    int[] t= new int[2];
    Integer k=0;
    IMapController mapController;
    private MapView map;

    Double lastLat=0.00;
    Double lastLon=0.00;
    Long lastTime;
    Integer i=0;
    SimpleLocationOverlay personOverlay;

    Thread oneM2MGPSThread;

    //MQTT String and variables
    MqttAndroidClient onem2m,onem2m2;
    String oneM2MVRUAeRi = "Csmartcampus";
    String oneM2MVRUAeRn = "aeSmartCampus1";
    String oneM2MVRUAePass = "smartcampuspassword";
    String oneM2MVRUReqTopic = "/oneM2M/req/aeSmartCampus1/server/json";
    private final static int CREATE = 1;
    private final static int RETRIEVE = 2;
    private final static int UPDATE = 3;
    private final static int DELETE = 4;
    String CsmartcampusSubscriptionTopic = "/oneM2M/resp/server/aeSmartCampus1/json";
    String CsmartCampusCarsSubscriptionTopic = "/oneM2M/resp/server/Ctechnolution/json";
    BroadcastReceiver broadcastReceiver;
    String userActivityType;
    int userActivityTypeInt=20;
    int userConfidence = 100;
    Long mActivityRecognitionTimestamp;
    SimpleDateFormat df;
    OneM2MMqttJson VRUgps;
    String huaweiUrl = "http://217.110.131.79:2020/mobile/dataapp";
    JSONObject contentCreateGPS, contentCreateUserStatus, contentCreateCallCar;

    //Notification global variables
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    int carNotificationConstant=0;
    int carNotificationConstant2 =0;
    Double carLon = 5.623863;
    Double carLat = 51.475792;

    //Logging
    File logFile;
    File file;
    String UTCPacketLossCheck;
    int packetLosses;

    //Car notifications
    Uri AUTONOMOUS_CAR_25M_NOTIFICATION_SOUND;
    private final static String AUTONOMOUS_CAR_40M_NOTIFICATION = "There is an autonomous car driving within 40 meters of your location!";
    private final static String AUTONOMOUS_CAR_100M_NOTIFICATION = "There is an autonomous car driving within 100 meters of your location!";
    private final static String AUTONOMOUS_CAR_NOTIFICATION_TITLE = "Autonomous car warning";
    private final static int AUTONOMOUS_CAR_40M_NOTIFICATION_ID = 0;
    private final static int AUTONOMOUS_CAR_100M_NOTIFICATION_ID = 1;
    Boolean[] notificationArray = new Boolean[10];
    Float carBearing;
    LatLng carLoc;

    //Car marker
    GroundOverlay carOverlay;
    Bitmap b;

    //Holding one gps location
    boolean isAlreadyHeld = false;
    boolean gpsHoldButtonChecked = false;
    Double Longitude=0.00000;
    Double Latitude=0.00000;
    Float Accuracy= Float.valueOf(0);

    //Local user and pass storage
    private String userId;
    String password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AUTONOMOUS_CAR_25M_NOTIFICATION_SOUND =  Uri.parse("android.resource://"+ getPackageName() + "/" + R.raw.translate_tts);
        Arrays.fill(notificationArray,false);
        super.onCreate(savedInstanceState);

        //Get user intent
        password  = getIntent().getStringExtra("password");
        userId = getIntent().getStringExtra("userId");
        if (savedInstanceState != null) {
            userId = savedInstanceState.getString("userId");
            password = savedInstanceState.getString("password");
        }




        //Authentication check
        FirebaseUser user = mAuth.getCurrentUser();
        userCheck(user);
        //getUsername(user);
        Log.d(TAG, "onCreate: " + userId);
        setContentView(R.layout.activity_gps);

        //Google maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.caricon);
        b=bitmapdraw.getBitmap();


        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Drawable locButtondrawableBefore = ContextCompat.getDrawable(getApplicationContext(), R.drawable.buttonshapebefore);
        Log.d(TAG, "onCreate: ");


        setupBottomNavigationBar();

        // GPS functionality, maybe in thread, maybe not, APP keeps doing thread after App quits.
        //TODO fix GPS function continueing after onDestroy when in thread

        //Hyperlog
        HyperLog.initialize(this);
        HyperLog.setLogLevel(Log.DEBUG);
        file = HyperLog.getDeviceLogsInFile(this);
        File path = getApplicationContext().getFilesDir();
        logFile = new File(path,"log.txt");


        oneM2MGPSThread = new Thread(new Runnable() {
            @Override
            public void run() {
                createLocationRequest();
                buildLocationSettingsRequest();
                createLocationCallback();

                setBroadcastReceiver();
                onem2m = buildOneM2MVRU(onem2m,userId);
                startTracking();

            }
        });
        oneM2MGPSThread.start();

        //Notification builder
        buildCarNotification(AUTONOMOUS_CAR_NOTIFICATION_TITLE);

        //setupMap();

        createVRUJSONS();
        drawable1 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.snackbarshape);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        viewLatitude = (TextView) findViewById(R.id.latitude);
        viewLongitude = (TextView) findViewById(R.id.longitude);
        viewBearing = (TextView) findViewById(R.id.bearing);
        viewLocation = (TextView) findViewById(R.id.location);
        viewSpeed = (TextView) findViewById(R.id.speed);
        gpsHolder = findViewById(R.id.holdgpsswitch);
      /*  gpsHolder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    gpsHoldButtonChecked = true;
                }
                else if (!b){
                    gpsHoldButtonChecked = false
                }

            }
        }); */

        updateValuesFromBundle(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .build();
        mGoogleApiClient.connect();

        df = new SimpleDateFormat("yyyyMMddHHmmssSS");
        //GPS functionality

        //OneM2M MQTT client
        Boolean a = checkPermissions();
        Boolean timerOn = false;

        if (a == false) {
            requestPermission();
        }
        if (a == true) {

        }
    }

    private void setupBottomNavigationBar() {
        //BottomNavigationBar
        final BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_campus:
                                removeFragments();
                                break;
                            case R.id.action_car:
                                android.app.Fragment fragment = new CampusCar();
                                switchToFragment(fragment);
                                break;
                            case R.id.action_settings:
                                break;
                        }
                        return true;
                    }
                });
    }

    private void removeFragments() {
        android.app.FragmentManager fm = getFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();}
    }

    private void switchToFragment(Fragment fragment) {
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.map, fragment).addToBackStack(null).commit();
    }


    private void setupCarOverlay() {
        carOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .position(new LatLng(50.967455, 5.943757),4)
                .image(BitmapDescriptorFactory.fromBitmap(b))
                .bearing(315));
    }

    private void locationIconUpdate(LatLng loc, Float carBearing) {
        if (carOverlay != null) {
            carOverlay.remove();
            carOverlay.setPosition(loc);
            carOverlay.setBearing(carBearing);
            carOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                    .position(loc,4)
                    .image(BitmapDescriptorFactory.fromBitmap(b))
                    .bearing(carBearing));
        }
    }

    @Override
    protected void onPause() {
        uploadFileFirebase();
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        /*try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.custom_maps));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }*/
        mMap.setMyLocationEnabled(true);

        setupCarOverlay();

        //Add buildings to map
        LatLngBounds flux = new LatLngBounds(
                new LatLng(51.447233, 5.491179),       // South west corner
                new LatLng(51.448241, 5.492668));      // North east corner

        GroundOverlayOptions fluxMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.flux_building))
                .positionFromBounds(flux)
                .clickable(true);

        fluxOverlay = mMap.addGroundOverlay(fluxMap);

        LatLngBounds metaforum = new LatLngBounds(
                new LatLng(51.446867, 5.486931),       // South west corner
                new LatLng(51.448223, 5.488004));      // North east corner

        GroundOverlayOptions metaforumMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.metaforum_building))
                .positionFromBounds(metaforum)
                .clickable(true);

        metaforumOverlay = mMap.addGroundOverlay(metaforumMap);


        LatLngBounds atlas = new LatLngBounds(
                new LatLng(51.447387, 5.485367),       // South west corner
                new LatLng(51.448349, 5.486638));      // North east corner

        GroundOverlayOptions atlasMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.atlas_building))
                .positionFromBounds(atlas)
                .clickable(true);

        atlasOverlay = mMap.addGroundOverlay(atlasMap);

        LatLngBounds auditorium = new LatLngBounds(
                new LatLng(51.447691, 5.483843),       // South west corner
                new LatLng(51.448102, 5.485029));      // North east corner

        GroundOverlayOptions auditoriumMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.auditorium_building))
                .positionFromBounds(auditorium)
                .clickable(true);

        auditoriumOverlay = mMap.addGroundOverlay(auditoriumMap);


        LatLngBounds vertigo = new LatLngBounds(
                new LatLng(51.445695, 5.484804),       // South west corner
                new LatLng(51.446751, 5.485260));      // North east corner

        GroundOverlayOptions vertigoMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.vertigo_building))
                .positionFromBounds(vertigo)
                .clickable(true);

        vertigoOverlay = mMap.addGroundOverlay(vertigoMap);

        //Clicklistener
        GoogleMap.OnGroundOverlayClickListener listener = new GoogleMap.OnGroundOverlayClickListener() {
            @Override
            public void onGroundOverlayClick(GroundOverlay groundOverlay) {
                if(groundOverlay.getId().equals(fluxOverlay.getId())){
                    //Action for flux
                    Log.d(TAG, "This is flux");

                    int height = 200;
                    int width = 200;
                    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.flux_building);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Flux = mMap.addMarker(new MarkerOptions()
                            .position(FLUX)
                            .title("Flux")
                            .snippet("Applied Physics and Electrical Engineering")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .alpha(0.1f));

                }
                else if(groundOverlay.getId().equals(vertigoOverlay.getId())){
                    //Action for vertigo
                    Log.d(TAG, "This is vertigo");

                    int height = 200;
                    int width = 200;
                    BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.vertigo_building);
                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Vertigo =  mMap.addMarker(new MarkerOptions()
                            .position(VERTIGO)
                            .title("Vertigo")
                            .snippet("Architecture")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .alpha(0.1f));
                }
                else if(groundOverlay.getId().equals(auditoriumOverlay.getId())){
                    //Action for auditorium
                    Log.d(TAG, "This is auditorium");

                    int height = 200;
                    int width = 200;
                    BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.auditorium_building);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Flux =  mMap.addMarker(new MarkerOptions()
                            .position(AUDITORIUM)
                            .title("Auditorium")
                            .snippet("Main Lecture Hall")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .alpha(0.1f));
                }
                else if(groundOverlay.getId().equals(metaforumOverlay.getId())){
                    //Action for metaforum
                    Log.d(TAG, "This is metaforum");

                    int height = 200;
                    int width = 200;
                    BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.metaforum_building);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Flux =  mMap.addMarker(new MarkerOptions()
                            .position(METAFORUM)
                            .title("Metaforum")
                            .snippet("Library")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .alpha(0.1f));
                }
                else if(groundOverlay.getId().equals(atlasOverlay.getId())){
                    //Action for atlas
                    Log.d(TAG, "This is atlas");

                    int height = 200;
                    int width = 200;
                    BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.atlas_building);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Flux =  mMap.addMarker(new MarkerOptions()
                            .position(ATLAS)
                            .title("Atlas")
                            .snippet("Industrial Design and Industrial Engineering")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .alpha(0.1f));
                }
                else if(groundOverlay.getId().equals(carOverlay.getId())){
                    //Action for car click
                    Log.d(TAG, "This is a car");

                    int height = 200;
                    int width = 200;
                    BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.flux_building);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    Flux =  mMap.addMarker(new MarkerOptions()
                            .position(FLUX)
                            .title("Flux")
                            .snippet("Applied Physics and Electrical Engineering")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .alpha(0.1f));
                }
            }
        };
        mMap.setOnGroundOverlayClickListener(listener);
    }

    private void buildCarNotification(String title) {
        long[] vibrationPattern = {Long.valueOf(0),Long.valueOf(500)};
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setVibrate(vibrationPattern)
                .setOngoing(true);// notification cannot be removed user
        Intent intent = new Intent(getApplicationContext(), GpsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

    }

    private void handleCarNotification(Double deltaMeters) {
        if (deltaMeters <= 40 && !notificationArray[AUTONOMOUS_CAR_40M_NOTIFICATION_ID]) {
            cancelNotification(AUTONOMOUS_CAR_100M_NOTIFICATION_ID);
            if(carNotificationConstant==0){
                mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .setSound(AUTONOMOUS_CAR_25M_NOTIFICATION_SOUND)
                        .setContentText(AUTONOMOUS_CAR_40M_NOTIFICATION)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setAutoCancel(false);
                mNotificationManager.notify(AUTONOMOUS_CAR_40M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_40M_NOTIFICATION_ID] = true;
                carNotificationConstant=1;}
            else if(carNotificationConstant==1){
                mBuilder.setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVibrate(null)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(AUTONOMOUS_CAR_40M_NOTIFICATION));
                mNotificationManager.notify(AUTONOMOUS_CAR_40M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_40M_NOTIFICATION_ID] = true;
            }
        }
        if(deltaMeters >= 40 && deltaMeters <= 100 && !notificationArray[1]) {
            cancelNotification(AUTONOMOUS_CAR_40M_NOTIFICATION_ID);
            if(carNotificationConstant2==0){
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(AUTONOMOUS_CAR_100M_NOTIFICATION));
                mBuilder.setSound(null);
                mNotificationManager.notify(AUTONOMOUS_CAR_100M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_100M_NOTIFICATION_ID]=true;}
            else if(carNotificationConstant2==1){
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(AUTONOMOUS_CAR_100M_NOTIFICATION));
                mBuilder.setSound(null);
                mNotificationManager.notify(AUTONOMOUS_CAR_100M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_100M_NOTIFICATION_ID]=true;}
        }
        if(deltaMeters>100) {
            cancelNotification(AUTONOMOUS_CAR_40M_NOTIFICATION_ID);
            cancelNotification(AUTONOMOUS_CAR_100M_NOTIFICATION_ID);
        }
        if(deltaMeters>48){
            carNotificationConstant=0;
        }
        if(deltaMeters>100){
            carNotificationConstant2=0;
        }
    }

    public void cancelNotification(int id) {
        if(notificationArray[id]){
            mNotificationManager.cancel(id);
            notificationArray[id]=false;}
    }

    private void createVRUJSONS() {
        VRUgps = new OneM2MMqttJson(oneM2MVRUAeRi,oneM2MVRUAePass,oneM2MVRUAeRn,userId);
        try {
            contentCreateGPS = VRUgps.CreateContentInstanceGps(null,null,null,null);
            contentCreateUserStatus = VRUgps.CreateContentInstanceStatus(null ,null,0);
            String to = "/server/server/aeSmartCampus1/Users/" + userId + "/Status";
            contentCreateUserStatus.getJSONObject("m2m:rqp").put("to",to);


            contentCreateCallCar = VRUgps.CreateContentInstanceCallTaxi(null,null,0,userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setBroadcastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ConstantsClassifier.ACTIVITY_BROADCAST_ACTION)) {
                    userActivityTypeInt = intent.getIntExtra("type",20);
                    userConfidence= intent.getIntExtra("confidence", 0);
                    Long Timestamp = intent.getLongExtra("timestamp",0);

                    userActivityType = checkActivityType(userActivityTypeInt);
                    mActivityRecognitionTimestamp = Timestamp;
                    Log.d(TAG, userActivityType + "From broadcast " + userConfidence);
                    if(userActivityTypeInt!=20){
                        try {
                            publishUserStatus(userActivityType,mActivityRecognitionTimestamp,userConfidence);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }}
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter(ConstantsClassifier.ACTIVITY_BROADCAST_ACTION));
    } // Recieves broadcast from DetectedActivitiesIntentService, which sends only highest confidence UserActivity

    private String checkActivityType(int type){
        switch(type){
            case DetectedActivity.IN_VEHICLE: {
                return "driving";
            }
            case DetectedActivity.ON_BICYCLE:{
                return "bicycling";
            }
            case DetectedActivity.ON_FOOT:{
                return "on foot";
            }
            case DetectedActivity.RUNNING:{
                return "running";
            }
            case DetectedActivity.WALKING:{
                return "walking";
            }
            case DetectedActivity.UNKNOWN:{
                return "unknown";
            }
            case DetectedActivity.STILL:{
                return "still";
            }
            default:{ return "unknown";}
        }

    }

    private void startTracking(){
        Intent intent1 = new Intent(GpsActivity.this, BackgroundDetectedActivitiesService.class);
        Log.d(TAG, "startTracking: ");
        startService(intent1);
    } // Starts the tracking of UserActivity via BackgroundDetectedAcitiviesService, which sends activities list to DetectedActivitiesIntentService, this gets the highest confidense activity type and broadcasts to setBroadcastReceiver

    private MqttAndroidClient buildOneM2MVRU(MqttAndroidClient mMqttAndroidClient, String userId1) {
        userId1 = userId1.replace("s","suser");
        String mqttBrokerUrl = "tcp://vmi137365.contaboserver.net:1883";
        mMqttAndroidClient = getMqttClient(getApplicationContext(), mqttBrokerUrl, userId);
        mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                subscribeToTopic(CsmartcampusSubscriptionTopic);
                subscribeToTopic(CsmartCampusCarsSubscriptionTopic);

                if (reconnect) {
                    Log.d(TAG, ("Reconnected to : " + serverURI));
                    subscribeToTopic(CsmartcampusSubscriptionTopic);
                    subscribeToTopic(CsmartCampusCarsSubscriptionTopic);
                } else {
                    Log.d(TAG,"Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG,"The Connection was lost." + cause.toString());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Log.d(TAG,"Incoming message: " + new String(message.getPayload()));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "delivery completed");
            }
        });
        return mMqttAndroidClient;
    } // Builds the OneM2M broker connection, subscribes to the VRU ae Response topic and creates UserID container.

    public void subscribeToTopic(String subscription) {
        try {
            onem2m.subscribe(subscription, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to subscribe");
                }

            }, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String lastTimeUTC = UTCPacketLossCheck;
                    Long timeUnix = System.currentTimeMillis();
                    oneM2MMessagesHandler(topic,message,timeUnix,lastTimeUTC);

                }
            });
        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }//Subscribes to response topic

    private void oneM2MMessagesHandler(String topic, MqttMessage message, Long timeUnix, String lastTimeUTC) throws JSONException {
        JSONObject messageCar = new JSONObject(new String(message.getPayload()));
        Log.d(TAG, "messageArrived: " + messageCar);
        if(topic.equals(CsmartCampusCarsSubscriptionTopic)){
            String contentCarString = messageCar.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONArray("m2m:sgn").getJSONObject(0).getJSONObject("nev").getJSONObject("rep").getJSONObject("m2m:cin").getString("con");
            String[] separated = contentCarString.split(",");

            String longitudeCarString = separated[3];
            String[] carLonseparated = longitudeCarString.split(":");
            carLon = Double.parseDouble(carLonseparated[1]);

            String bearing = separated[6];
            String[] bearingSep = bearing.split(":");
            Toast.makeText(getApplicationContext(),bearingSep[1],Toast.LENGTH_LONG).show();

            //String bearing1[] = bearingSep[1].split("}");
            //carBearing = Float.parseFloat(bearing1[0]);



            //String speedCarString = separated[5];
            //String[] speedCarSep = speedCarString.split(":");
            //Double speedCar = Double.parseDouble(speedCarSep[1]);
            // String headingCar = separated[6];

            String latitudeCar = (separated[4]);
            String[] carLatseparated = latitudeCar.split(":");
            carLat = Double.parseDouble(carLatseparated[1]);
            carLoc = new LatLng(carLat,carLon);
            if(mCurrentlocation!=null){
                Double deltaMeters;
                deltaMeters = DifferenceInMeters(carLat,carLon,mCurrentlocation.getLatitude(),mCurrentlocation.getLongitude());
                Log.d(TAG, "Deltameter:" + deltaMeters);
                handleCarNotification(deltaMeters);
            }
        }
        else if(topic.equals(CsmartcampusSubscriptionTopic)){
            if(messageCar.getJSONObject("m2m:rsp").getString("rqi").equals(userId)){
                String contentTimeString = messageCar.getJSONObject("m2m:rsp").getJSONObject("pc").getJSONArray("m2m:cin").getJSONObject(0).getString("rn");
                //if(!contentTimeString.equals(lastTimeUTC)){packetLosses++;}
                Long timeGps = Long.parseLong(contentTimeString);
                Long deltaTime = timeUnix - timeGps;
                String latencyFromGPSTillReceive = Long.toString(deltaTime);
                Log.d(TAG, "Latency:" + latencyFromGPSTillReceive);
                String[] fileContents = {contentTimeString,latencyFromGPSTillReceive,Integer.toString(packetLosses)};
                writeToLogFile(fileContents);
            }
        }
    } //Processes the messages that are incoming

    public void publishMessage(@NonNull MqttAndroidClient client, @NonNull String msg, int qos, @NonNull String topic) throws MqttException, UnsupportedEncodingException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(5866);
        message.setRetained(true);
        message.setQos(qos);
        HyperLog.d(TAG, "Sent message: " + new String(message.getPayload()));
        Log.d(TAG, "Sent message: " + new String(message.getPayload()));
        client.publish(topic, message).setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                packetLosses++;
            }
        });
    } // Publishes message to VRU ae on OneM2M

    private void writeToLogFile(String[] entry) {
        String FILENAME = userId + "-" + "OneM2MBackAndForthLatency.csv";
        StringBuilder stringBuilder = new StringBuilder();

        //Array loops all sring entries and seperates by comma as in CSV file
        for(String string : entry){
            String strTemp = string + ',';
            stringBuilder.append(strTemp);
        }
        String entryFile = stringBuilder.toString() + "\n";
        try {
            FileOutputStream out = openFileOutput(FILENAME,Context.MODE_APPEND);
            out.write(entryFile.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG,"writeToLogFile" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "writeToLogFile" + e.toString());
        }
    } //Writes a logfile or appends this file if it is already existing with an arbitrary array (does not matter how large) seperated by commas. (Time,Latency, ..... Lat, Lon) will be one line in an CSV file to excel.

    public MqttAndroidClient getMqttClient(@NonNull Context context,@NonNull String brokerUrl, @NonNull String clientId) {
        final MqttAndroidClient mqttClient = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = mqttClient.connect(getMqttConnectionOption());
            if(token==null){Log.d(TAG, "token is null");}
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //TODO set custom disconnect options onem2m.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "getMqttClient: Success");
                    OneM2MMqttJson VRU = new OneM2MMqttJson(oneM2MVRUAeRi, oneM2MVRUAePass, oneM2MVRUAeRn,userId);
                    subscribeToTopic(CsmartcampusSubscriptionTopic);
                    subscribeToTopic(CsmartCampusCarsSubscriptionTopic);
                    try {
                        publishMessage(onem2m,VRU.CreateContainer(userId).toString(),0,oneM2MVRUReqTopic);
                        publishMessage(onem2m,VRU.CreateUserContainer("Gps").toString(),0,oneM2MVRUReqTopic);
                        publishMessage(onem2m,VRU.CreateUserContainer("Status").toString(),0,oneM2MVRUReqTopic);
                        publishMessage(onem2m,VRU.CreateUserContainer("CallTaxi").toString(),0,oneM2MVRUReqTopic);
                        publishMessage(onem2m,VRU.CreateTaxiSubContainer().toString(),0,oneM2MVRUReqTopic);
                    } catch (JSONException e) {
                        e.printStackTrace();}
                    catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "getMqttClient: Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return mqttClient;
    } // Initialize MQTT client

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName("autopilot");
        mqttConnectOptions.setPassword("onem2m".toCharArray());
        mqttConnectOptions.setKeepAliveInterval(60);
        return mqttConnectOptions;
    } //Options for MQTT client (Clean session, automatic reconnect etc)

    @Override
    public void processFinish(String output) {
        Log.d(TAG, "processFinish: "+ output);
    } //Handler voor response van de asynctask post OkHTTP

    void okHTTPPost(String url, String json) {
        okHttpPost post1 = new okHttpPost(this);
        String[] string = new String[2];
        string[0]=url;
        string[1]=json;
        post1.execute(string);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        uploadFileFirebase();
        super.onStop();
    }

    private void setupMap() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(16);
        GeoPoint startPoint =  new GeoPoint(51.448765, 5.489602);
        mapController.setCenter(startPoint);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if(checkPermissions()){
            startLocationUpdates();
        }
        else if(!checkPermissions()){
            requestPermission();
        }
    }
    private void updateLocationUI(Long timestampUTC) {
        if(!gpsHolder.isChecked()){
            isAlreadyHeld=false;
        }

        if(mCurrentlocation!=null){

            //Getting info from mCurrentlocation
            if(carLoc!=null){
            carOverlay.setPosition(carLoc);
            carOverlay.setBearing(carBearing);}

            if(gpsHolder.isChecked()) {
                if(!isAlreadyHeld){
                     Longitude = mCurrentlocation.getLongitude();
                     Latitude = mCurrentlocation.getLatitude();
                     Accuracy = mCurrentlocation.getAccuracy();
                    isAlreadyHeld=true;
                    Log.d(TAG, "updateLocationUI: !isAlreadyHeld");
                }
            }
            else{
             Longitude = mCurrentlocation.getLongitude();
             Latitude = mCurrentlocation.getLatitude();
             Accuracy = mCurrentlocation.getAccuracy();
                Log.d(TAG, "updateLocationUI: normal operation");
            }

            //Speed implementation
            String speedGPS;
            String[] speedGPSandBearing = calculateSpeedandBearingandImplementPolyline(Latitude,Longitude,timestampUTC);

            if(mCurrentlocation.hasSpeed()){speedGPS=Float.toString(mCurrentlocation.getSpeed());
            }
            else{
                speedGPS = speedGPSandBearing[0];}
            if(mCurrentlocation.hasBearing()){bearing = Float.toString(mCurrentlocation.getBearing());}
            else{
                bearing = speedGPSandBearing[1];}

            //Sending GPS to oneM2M
            try {
                publishGpsData(Latitude,Longitude,Accuracy, timestampUTC,speedGPS,bearing);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSS");


            Double deltaMeters;

            //Google maps camera

            if(k==0){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Latitude,Longitude)));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(19));
                k=1;}


            deltaMeters = DifferenceInMeters(carLat,carLon,mCurrentlocation.getLatitude(),mCurrentlocation.getLongitude());
            handleCarNotification(deltaMeters);



            String Bearing = Double.toString(mCurrentlocation.getBearing());
            Log.d(TAG, "Accuracy: " + Accuracy + " Bearing: " + Bearing);

            latitude = String.format(Locale.ENGLISH,"%f", mCurrentlocation.getLatitude());
            longitude = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getLongitude());
            longitude = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getLongitude());
            bearing = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getBearing());
            if(Build.VERSION.SDK_INT>=26){
                bearingAccuracy = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getBearingAccuracyDegrees());}
            Log.d(TAG, "bearing: " + bearing + " bearingAccuracy: " + bearingAccuracy);
            speed = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getSpeed());
            viewLatitude.setText(latitude);
            viewLongitude.setText(longitude);
            viewBearing.setText(bearing);
            //viewBearingAccuracy.setText(bearingAccuracy);

            /*Integer timeDifference = 0;
            timeDifference = mGetTimeDifference();
            mDatabase= FirebaseDatabase.getInstance().getReference();
            String timeDifferenceString = Integer.toString(timeDifference);
            String firebaseString = latitude + "    " + longitude + "   " + timeDifferenceString;
            String gpsCount = Integer.toString(k);
            mDatabase.child("GPSTest").child(userId).child(gpsCount).setValue(firebaseString, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            k++;
                        }
                    });
            */

            viewSpeed.setText(speedGPS);
            //viewmanualBearing.setText(bearing);


            //Data to Firebase, not needed atm
            /*mDatabase.child("users").child(userId).child("longitude").setValue(longitude);
            mDatabase.child("users").child(userId).child("latitude").setValue(latitude);
            mDatabase.child("users").child(userId).child("speed").setValue(speed);*/

            //Publishing gps to onem2m


            //UI buttons and on campus test
            GeoPoint loc = new GeoPoint(Latitude,Longitude);
            //String location = "lat: " + latitude + " lng: " + longitude;
            /*Double bound1la = 51.445110;
            Double bound2la= 51.452770;
            Double bound1lo = 5.500690;
            Double bound2lo = 5.481070;

            onCampusTest(bound1la,bound2la,bound2lo,bound1lo, Longitude, Latitude);*/
            //personIconUpdate(loc);
            // myLocationButton(loc);
            // buildingIcon(loc);


        }
    }

    /*private void makePolyline(List<GeoPoint> geoPoints,Polyline polyline) {
        if(headingLine!=null){
            map.getOverlays().remove(headingLine);
        }
        polyline= new Polyline();
        polyline.setPoints(geoPoints);
        map.getOverlayManager().add(polyline);
        map.invalidate();
    }
*/
    private void publishUserStatus(String activity, Long timeStamp, int confidence) throws JSONException, MqttException, UnsupportedEncodingException {
        String con = "activity: " + activity +  "," +  " activity confidence: " + confidence;
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("con", con);
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("con", con);
        String to = "/server/server/aeSmartCampus1/Users/" + userId + "/Status";
        contentCreateUserStatus.getJSONObject("m2m:rqp").put("to",to);
        String contentCreateStatus = contentCreateUserStatus.toString();
        publishMessage(onem2m,contentCreateStatus,0,oneM2MVRUReqTopic);}

    private void publishGpsData(Double latitude, Double longitude, Float Accuracy, Long formattedDate, String speedGPS, String manualBearing) throws JSONException, MqttException, UnsupportedEncodingException {
        String formattedDateString = "UTC"+ Long.toString(formattedDate) ;
        UTCPacketLossCheck = formattedDate.toString();
        String topic = "/server/server/" + "aeSmartCampus1" + "/Users/" + userId + "/gps";
        String con = "{\"type\":5,\"id\":" + userId + ", \"timestampUtc\":" + formattedDate + ", \"lon\":" + longitude + ", \"lat\":"+ latitude + ", \"speed\":"+ speedGPS + ", \"heading\":"+manualBearing+ ", \"accuracy\":"+Accuracy+ "}";
        String conHuawei = "{\"type\":5,\"id\":" + userId + ", \"timestampUtc\":" + formattedDateString + ", \"lon\":" + longitude + ", \"lat\":"+ latitude + ", \"speed\":"+ speedGPS + ", \"heading\":"+manualBearing+ ", \"accuracy\":"+Accuracy+ "}";
        okHTTPPost(huaweiUrl,conHuawei);
        contentCreateGPS.getJSONObject("m2m:rqp").put("to",topic);
        contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("con", con);
        contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("rn", formattedDate);
        String contentCreate = contentCreateGPS.toString();
        publishMessage(onem2m,contentCreate,0,oneM2MVRUReqTopic);
    } //Publishes messages to onem2m broker by MQTT and posts to Huawei set up server via HTTP

    private String[] calculateSpeedandBearingandImplementPolyline(Double latitude, Double longitude, Long timeStamp){
        String speedGPS;
        String[] speedandBearing = new String[2];
        StringBuilder sb = new StringBuilder();
        if(lastLat == 0.00){
            lastLat=latitude;
            lastLon=longitude;
            lastTime = timeStamp;
            speedGPS="0.00";
        }
        else{
            Double deltaSeconds = DifferenceUTCtoSeconds(timeStamp,lastTime)/1000;
            Double deltaMeters = DifferenceInMeters(lastLat,lastLon,latitude,longitude);
            speedGPS = Double.toString(deltaMeters/deltaSeconds);
            String bearingGPS = Double.toString(ManualBearing(lastLat,lastLon,latitude,longitude));
            Log.d(TAG, "manualbearing: " + bearingGPS);
            speedandBearing = new String[2];
            speedandBearing[0] = speedGPS;
            speedandBearing[1] = bearingGPS;
            GeoPoint lastGeo = new GeoPoint(lastLat,lastLon);
            GeoPoint newGeo = new GeoPoint(latitude,longitude);
            List<GeoPoint> Geopoints = new ArrayList<>();
            Geopoints.add(lastGeo);
            Geopoints.add(newGeo);
            // makePolyline(Geopoints,headingLine);
            lastLat=latitude;
            lastLon=longitude;
            lastTime=timeStamp;

        }
        //TODO add a realistic threshold to prevent huge speeds at delta t goes to zero

        return speedandBearing;
    } //Makes a line on the map between the last two points

    private double DifferenceUTCtoSeconds(Long timeStamp, Long timeStamp2){
        /*Double deltaseconds = Double.parseDouble(new String(new char[]{timeStamp.charAt(12),timeStamp.charAt(13)}))-Double.parseDouble(new String(new char[]{timeStamp2.charAt(12),timeStamp2.charAt(13)}));
        Double deltamiliseconds = Double.parseDouble(new String(new char[]{timeStamp.charAt(14),timeStamp.charAt(15)}))-Double.parseDouble(new String(new char[]{timeStamp2.charAt(14),timeStamp2.charAt(15)}));
        Double deltaminutes = Double.parseDouble(new String(new char[]{timeStamp.charAt(10),timeStamp.charAt(11)}))-Double.parseDouble(new String(new char[]{timeStamp2.charAt(10),timeStamp2.charAt(11)}));
        Double deltahours = Double.parseDouble(new String(new char[]{timeStamp.charAt(8),timeStamp.charAt(9)}))-Double.parseDouble(new String(new char[]{timeStamp2.charAt(8),timeStamp2.charAt(9)}));
        Double deltadays = Double.parseDouble(new String(new char[]{timeStamp.charAt(6),timeStamp.charAt(7)}))-Double.parseDouble(new String(new char[]{timeStamp2.charAt(6),timeStamp2.charAt(7)}));
        Double deltamonths = Double.parseDouble(new String(new char[]{timeStamp.charAt(4),timeStamp.charAt(5)}))-Double.parseDouble(new String(new char[]{timeStamp2.charAt(4),timeStamp2.charAt(5)}));
        Double deltayears = Double.parseDouble(new String(new char[]{timeStamp.charAt(0),timeStamp.charAt(1), timeStamp.charAt(2),timeStamp.charAt(3)}))-Double.parseDouble(new String(new char[]{timeStamp2.charAt(0),timeStamp2.charAt(1),timeStamp2.charAt(2),timeStamp2.charAt(3)}));
        Double totalDeltaInMiliSeconds = deltaseconds*1000 + deltamiliseconds + deltaminutes * 60 * 1000 + deltahours *60*60*1000 + deltadays*24*60*60*1000; //This does not include difference in months since that is irrelevant for two gps time points
        return totalDeltaInMiliSeconds;*/
        return timeStamp-timeStamp2;
    }
    private double DifferenceInMeters(Double lastLat,Double lastLon,Double lat,Double lon){
        Double deltaPhiLon = (lon - lastLon)*Math.PI/180;
        //Log.d(TAG, "DiffMetersdeltaphilon: " + deltaPhiLon);
        Double deltaPhilat = (lat - lastLat)*Math.PI/180;
        lastLat = lastLat*Math.PI/180;
        lat = lat*Math.PI/180;

        Double earth = 6371e3;

        Double a  = Math.sin(deltaPhilat/2)*Math.sin(deltaPhilat/2)+Math.cos(lastLat)*Math.cos(lat)*Math.sin(deltaPhiLon)*Math.sin(deltaPhiLon);
        Double c  = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        Double d = earth*c;
        return d;
    } // Difference in meters (birds flight) using 'haversine' formula

    private double ManualBearing(Double lastLat,Double lastLon,Double lat,Double lon){
        //phi = lat, lambda = long
        Double deltaPhiLon = (lon - lastLon) * Math.PI/180;
        Double deltaPhilat = (lat - lastLat) * Math.PI/180;
        lastLon = lastLon*Math.PI/180;
        lon = lon*Math.PI/180;
        lastLat = lastLat*Math.PI/180;
        lat = lat*Math.PI/180;

        Double a = Math.sin(deltaPhiLon)*Math.cos(lat);
        Double b = Math.cos(lastLat) * Math.sin(lat) - Math.sin(lastLat) * Math.cos(lat) * Math.cos(deltaPhiLon);
        Double c = Math.atan2(a, b)*180/Math.PI;
        Double d;
        if(c < 0){
            d = c + 360;

        }
        else {
            d = c;
        }
        return d;
    }


    private void personIconUpdate(GeoPoint loc) {
        if(personOverlay!=null){
            map.getOverlays().remove(personOverlay);
        }

        Drawable vectorDrawable = ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.locationicon, null);
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.locationicon);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap locationIcon = Bitmap.createScaledBitmap(b, 40, 40, false);
        personOverlay= new SimpleLocationOverlay(locationIcon);
        personOverlay.setLocation(loc);
        map.getOverlays().add(personOverlay);
        map.invalidate();
    }




    private void onCampusTest(Double bound1la, Double bound2la, Double bound2lo, Double bound1lo, Double Longitude, Double Latitude) {
        if(Latitude>bound1la && Latitude<bound2la && Longitude<bound1lo && Longitude>bound2lo){
            viewLocation.setText("You are currently on Tu/e campus");
            mDatabase.child("users").child(userId).child("onTue").setValue("yes");}
        else{mDatabase.child("users").child(userId).child("onTue").setValue("no");
            viewLocation.setText("You are currently not on Tu/e campus");}
    }

    @Override
    protected void onDestroy() {
        uploadFileFirebase();
        Log.d(TAG, "destroy");
        FirebaseAuth.getInstance().signOut();
        stopTracking();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        mNotificationManager.cancelAll();


        super.onDestroy();
    }

    private void uploadFileFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String filePath = getApplicationContext().getFilesDir() + "/" + userId + "-" + "OneM2MBackAndForthLatency.csv";
        Uri file = Uri.fromFile(new File(filePath));
        StorageReference storageRef = storage.getReference();
        StorageReference fileReference = storageRef.child(file.getLastPathSegment());
        UploadTask uploadTask = fileReference.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "FailureStorage: " + exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Succes of storage");
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    } // Uploads file to firebase, currently called whenever the app is paused or destroyed.


    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INVTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    } // Constructor of locationrequest, sets certain settings of the locationrequest (interval, priority etc)

    private void myLocationButton(GeoPoint loc) {
        final GeoPoint locf = loc;
        Drawable locButtondrawableAfter = ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshape);
        /*Button locButton = (Button) findViewById(R.id.locButton);
        if(locButton.getBackground()!=locButtondrawableAfter){
        locButton.setBackground(locButtondrawableAfter);}
        if(locButton.getBackground()==locButtondrawableAfter){
            locButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map.getController().setCenter(locf);

                }
            });
        }*/
    }

    private void buildLocationSettingsRequest(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(this.mLocationRequest);
        mLocationSettingsRequest= builder.build();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userId",userId);
        outState.putString("password",password);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        stopTracking();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        this.finishAffinity();


    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(GpsActivity.this.mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                        Log.d(TAG, "onSuccess:");

                        updateLocationUI(System.currentTimeMillis());

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
                                Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
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
    } //Requests the user for permission of GPS

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
    } // Handler for permission results (if cancelled or accepted)

    private boolean checkPermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;

    } // Checks whether user gives permission to use GPS

    private void userCheck(FirebaseUser user) {
        if (user == null) {
            MainActivity Login =  new MainActivity();
            Login.login(userId+"@random.com",password);
            Log.d(TAG, "userCheck: User is null");
            Intent loginIntent = new Intent(GpsActivity.this, MainActivity.class);
            startActivity(loginIntent);
        }
    } //Checks if user is still logged in via Firebase

    private void getUsername(FirebaseUser user) {
        String userEmail = user.getEmail();
        userId = userEmail.replace("@random.com","");
    }
    // Gets the username from Firebase

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Long timestamp = System.currentTimeMillis();
                mCurrentlocation= locationResult.getLastLocation();
                String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI(timestamp);

            }
        };

    }

    private void buildingIcon(GeoPoint loc) {
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        OverlayItem locationIcon=new OverlayItem("Title", "Description", loc);
        items.add(locationIcon);

        /*BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.buildingicon);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap buildingIcon = Bitmap.createScaledBitmap(b, 60, 60, false);
        BuildingMarker = new SimpleLocationOverlay(buildingIcon);
        GeoPoint startPoint = new GeoPoint(51.447500, 5.491267);
        BuildingMarker.setLocation(startPoint);
        map.getOverlays().add(BuildingMarker);
        map.invalidate();

        Marker startMarker = new Marker(map);
        GeoPoint startPoint = new GeoPoint(51.447000, 5.491267);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        startMarker.setIcon(getResources().getDrawable(R.drawable.buildingicon));
        startMarker.setTitle("Flux"); */

        ItemizedOverlayWithFocus mItemizedOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                return false;
            }

            @Override
            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return false;
            }
        }, this);
        items.add(locationIcon);
        mItemizedOverlay.addItems(items);
        //TODO Add the icon in the map
    }

    private int mGetTimeDifference() {
        Integer timeInSeconds[] = new Integer[2];
        Integer timeDifference = 0;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dh = new SimpleDateFormat("hh");
        SimpleDateFormat dm = new SimpleDateFormat("mm");
        SimpleDateFormat ds = new SimpleDateFormat("ss");
        String formattedHours = dh.format(c.getTime());
        String formattedMinutes = dm.format(c.getTime());
        String formattedSeconds=  ds.format(c.getTime());
        Integer timeInHours[] = new Integer[2];
        Integer timeSeconds[]= new Integer[2];
        Integer timeInMinutes[]= new Integer[2];
        timeInHours[i] = Integer.parseInt(formattedHours);
        timeSeconds[i]=Integer.parseInt(formattedSeconds);
        timeInMinutes[i]=Integer.parseInt(formattedMinutes);
        String timeDifferenceString = Integer.toString(i);



        if(i==0){
            timeInSeconds[0]= timeInHours[0]*3600 +timeInMinutes[0]*60 + timeSeconds[0];}
        if(i==1){
            timeInSeconds[1]= timeInHours[1]*3600 +timeInMinutes[1]*60 + timeSeconds[1];
        }
        //timeDifference = timeInSeconds[1]-timeInSeconds[0];}

        return timeInSeconds[i];
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentlocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                String mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
        }
    } //Updates values from savedinstance

    private void stopTracking() {
        Intent intent = new Intent(GpsActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    } // Stops tracking of UserActivity (Called in OnDestroy)

    public void CallCar() {
        try {
            publishMessage(onem2m,VRUgps.CreateContentInstanceCallTaxi(mCurrentlocation.getLatitude(), mCurrentlocation.getLongitude(), System.currentTimeMillis(), userId).toString(),0,oneM2MVRUReqTopic);
        } catch (JSONException e) {
            Log.d(TAG, "CallCar: "+e.toString());
        } catch (UnsupportedEncodingException e){
            Log.d(TAG, "CallCar: "+e.toString());
        } catch (MqttException e) {
            Log.d(TAG, "CallCar: "+e.toString());
        }
    } //Sends a message to OneM2M CallCar container when button is clicked for calling a taxi. This message is then forwarded to Csmartcampus topic for IBM rebalancing service via the subscription container CallTaxi_sub

    //TODO Imageoverlay clickable for Tu campus
    //TODO Car data receive timestamp and warning display timestamp
    //TODO Extrapolating the vehicle speed heading if time is too long/delay
    //TODO randomize the ID (crossguid)  java.util.UUID id = UUID.randomUUID();
}
