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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;


public class GpsActivity extends AppCompatActivity implements MapViewConstants, okHttpPost.AsyncResponse, OnMapReadyCallback {

    String TAG = "GpsActivity";
    private Drawable permissionGrantedSnackbarShape;

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
    private Marker Atlas;
    private GroundOverlay auditoriumOverlay;
    private static final LatLng AUDITORIUM = new LatLng(51.447625, 5.484348);
    private Marker Auditorium;
    private GroundOverlay vertigoOverlay;
    private static final LatLng VERTIGO = new LatLng(51.445967, 5.484991);
    private Marker Vertigo;

    //Firebase variables
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Googleapi and location clients
    private FusedLocationProviderClient mFusedLocationClient;
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;

    //Layout elements
    private TextView viewLatitude;
    private TextView viewLongitude;
    private TextView viewLocation;
    private TextView viewBearing;
    private TextView viewSpeed;
    private Switch gpsHolder;
    private Location mCurrentlocation;

    //Location request settings constants
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private final static long UPDATE_INVTERVAL_IN_MILLISECONDS = 500;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1;


    //Initial values for speed and bearing calculations
    Double lastLat=0.00;
    Double lastLon=0.00;
    Long lastTime;

    Thread oneM2MGPSThread;

    //MQTT oneM2M login credentials, subscription and request topics and JSONs.
    MqttAndroidClient onem2m;
    String oneM2MVRUAeRi = "Csmartcampus";
    String oneM2MVRUAeRn = "aeSmartCampus1";
    String oneM2MVRUAePass = "smartcampuspassword";
    String oneM2MVRUReqTopic = "/oneM2M/req/aeSmartCampus1/server/json";
    String CsmartcampusSubscriptionTopic = "/oneM2M/resp/server/aeSmartCampus1/json";
    String CsmartCampusCarsSubscriptionTopic = "/oneM2M/resp/server/Ctechnolution/json";
    JSONObject contentCreateGPS, contentCreateUserStatus, contentCreateCallCar;
    //OneM2M op (operand) for in json op: fields.
    private final static int CREATE = 1;
    private final static int RETRIEVE = 2;
    private final static int UPDATE = 3;
    private final static int DELETE = 4;

    //DetectUserActivity variables
    BroadcastReceiver broadcastReceiver;
    String userActivityType;
    int userActivityTypeInt=20;
    int userConfidence = 100;
    long mActivityRecognitionTimestamp;
    OneM2MMqttJson VRUgps;

    //Notification global variables
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    int carNotificationConstant=0;
    int carNotificationConstant2 =0;
    double carLon = 5.623863;
    double carLat = 51.475792;
    Float carHeading=null;
    Float carSpeed=null;
    Long lastRTK=null;
    boolean noRTK = true;

    //Logging files
    File file;
    String UTCPacketLossCheck;
    int packetLosses;
    int experimentNumber = 0;
    int runNumber = 0;
    Vector<String> fileNameVector =  new Vector<>();

    //Message types for logging (what kind of log is needed)
    private final static int LOGGING_NOTNEEDED = 0;
    private final static int LOGGING_GPS = 1;
    private final static int LOGGING_STATUS = 2;
    private final static int LOGGING_VEHICLE = 3;
    private final static int LOGGING_HUAWEI_SENT = 4;
    private final static int LOGGING_HUAWEI_RECEIVED = 5;

    //Logging layout widgets
    EditText runNumberText, experimentNumberText;
    Switch loggingSwitch;

    //Huawei communication
    String huaweiUrl = "http://217.110.131.79:2020/mobile/dataapp";
    //Huawei timer
    Timer huaweiTimer;
    TimerTask huaweiTimerTask;

    //Car notifications
    Uri AUTONOMOUS_CAR_25M_NOTIFICATION_SOUND;
    private final static String AUTONOMOUS_CAR_40M_NOTIFICATION =
            "There is an autonomous car driving within 40 meters of your location!";
    private final static String AUTONOMOUS_CAR_100M_NOTIFICATION =
            "There is an autonomous car driving within 100 meters of your location!";
    private final static String AUTONOMOUS_CAR_NOTIFICATION_TITLE = "Autonomous car warning";
    private final static int AUTONOMOUS_CAR_40M_NOTIFICATION_ID = 0;
    private final static int AUTONOMOUS_CAR_100M_NOTIFICATION_ID = 1;
    private final static int HUAWEI_NOTIFICATION_ID = 3;
    Boolean[] notificationArray = new Boolean[10];
    LatLng carLoc;

    //Car marker
    GroundOverlay carOverlay;
    Bitmap carBitmap;
    com.google.android.gms.maps.model.Polygon geoFencingPolygon;
    com.google.android.gms.maps.model.Polygon speedPolygon;

    //Holding one gps location boolean
    boolean isAlreadyHeld = false;

    //userId and Password from Firebase as well as the stored versions.
    public String userName;
    String password;
    String passwordStored;
    String userNameStored;

    //GPS location global variables
    double Longitude;
    double Latitude;
    float Accuracy= Float.valueOf(0);
    public  String longitude;
    public  String latitude;
    public  String bearing;
    public  String bearingAccuracy;
    public String speed;

    //Request code for the permissions intent (asking for some permission)
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        //Sets orientation so the screen is locked to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Assigning of notification sound from downloaded google translate sound and filling of
        //notification array (fills up if notifications are active).
        AUTONOMOUS_CAR_25M_NOTIFICATION_SOUND =  Uri.parse("android.resource://"+ getPackageName() +
                "/" + R.raw.translate_tts);
        Arrays.fill(notificationArray,false);

        //Get data from intent from MainActivity
        password  = getIntent().getStringExtra("password");
        userName = getIntent().getStringExtra("userId");
        userNameStored = getIntent().getStringExtra("userNameStored");
        passwordStored = getIntent().getStringExtra("passwordStored");
        if (savedInstanceState != null) {
            userName = savedInstanceState.getString("userId");
            password = savedInstanceState.getString("password");
        }

        //Authentication check
        FirebaseUser user = mAuth.getCurrentUser();
        userCheck(user);

        //Sets layout to activity.gps xml layout
        setContentView(R.layout.activity_gps);

        //Assigning of logging layout elements
        runNumberText = (EditText) findViewById(R.id.runNumber);
        experimentNumberText = (EditText) findViewById(R.id.experimentNumber);
        experimentNumberText.clearFocus();
        runNumberText.clearFocus();
        loggingSwitch = (Switch) findViewById(R.id.logSwitch);

        //Google maps support fragment assignment and intialization
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Bottom navigation bar
        setupBottomNavigationBar();

        //starts the oneM2M and location client thread, also involves the starting of user detection.
        startOneM2MGpsUserActivityThread();

        //Notification builder
        buildCarNotification(AUTONOMOUS_CAR_NOTIFICATION_TITLE);

        createVRUJSONS();
        permissionGrantedSnackbarShape = ContextCompat.getDrawable(getApplicationContext(), R.drawable.snackbarshape);

        //Firebase initialization
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        //Left upper corner of layout widget assignment (textviews for lat, lon, speed etc).
        viewLatitude = (TextView) findViewById(R.id.latitude);
        viewLongitude = (TextView) findViewById(R.id.longitude);
        viewBearing = (TextView) findViewById(R.id.bearing);
        viewLocation = (TextView) findViewById(R.id.location);
        viewSpeed = (TextView) findViewById(R.id.speed);
        gpsHolder = findViewById(R.id.holdgpsswitch);

        updateValuesFromBundle(savedInstanceState);

        //Assignment of global variables for google location client after starting it in the GPS
        // thread. Also connects to the ActivityRecognition api needed for Activity detection.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .build();
        mGoogleApiClient.connect();

        // Huawei dummy message poster for faster rectangle updates
        huaweiTimer();

        // Checks permissions at end of onCreate as a safety measure (should have been requested
        // already by GPS oneM2M thread).
        if (checkPermissions()) {
            requestPermission();
        }
    }

    // Starts location request and updates as well as the oneM2M connection and communication.
    // Moreover it starts the detection of user activity.
    private void startOneM2MGpsUserActivityThread() {

        oneM2MGPSThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Creates settings to be passed to google's fusion location client and sets callback
                //This is the core of the app
                createLocationRequest();
                buildLocationSettingsRequest();
                createLocationCallback();

                //Initiates DetectedUserActivity api from google and sets receiver for broadcast
                //whenever an activity is detected and sent back from that thread
                setBroadcastReceiver();
                startTrackingUserActivity();

                //Onem2m building of broker
                onem2m = buildOneM2MVRU(onem2m,userName);

            }
        });
        oneM2MGPSThread.start();
    }

    private void huaweiTimer(){
        huaweiTimer = new Timer();
        initializeHuaweiTimerTask();
        huaweiTimer.schedule(huaweiTimerTask,10000, 400);
    }

    private void initializeHuaweiTimerTask() {
        huaweiTimerTask =  new TimerTask() {
            @Override
            public void run() {
                String conHuawei = "{\"type\":5,\"id\":" + "fenceRequester" + ",\"timestampUtc\":" +
                        0 + ",\"lon\":" + 0 + ",\"lat\":"+ 0 + ",\"speed\":"+ 0 + ",\"heading\":"+0+ ",\"accuracy\":"+0+ "}";
                okHTTPPost(huaweiUrl,conHuawei);
            }
        };
    }

    //Assigns listeners to when the buttom navigation is clicked, changing activity etc.
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
    } //TODO make campusCar an activity instead of fragment

    // Removes all fragments that are on the stack, fragments are stored on top of eachother on a
    // stack (sort of memory) and can be popped (removed), this goes back to initial google maps
    // fragment.
    private void removeFragments() {
        android.app.FragmentManager fm = getFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();}
    }

    // Switches the fragmentcontainer which contains google maps (initially) to a certain other
    // fragment, this is passed to this function from its implementation.
    private void switchToFragment(Fragment fragment) {
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.map, fragment).addToBackStack(null).commit();
    }

    @Override
    protected void onPause() {
        if(fileNameVector!=null){uploadLogFilesFirebase();}
        super.onPause();
    }

    //Override function called when google maps is first initialized/ready in the app. All campus
    // buildings are initialized (their actions/listeners) as well as the car overlay and user
    // location that is observable in the app.
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

        if (checkPermissions()) {
            mMap.setMyLocationEnabled(true);
        } else while (!checkPermissions()) {
            requestPermission();
        }
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

        GoogleMap.OnGroundOverlayClickListener listener = new GoogleMap.OnGroundOverlayClickListener() {
            @Override
            public void onGroundOverlayClick(GroundOverlay groundOverlay) {
                if (groundOverlay.getId().equals(fluxOverlay.getId())) {
                    //Action for flux

                    Flux = mMap.addMarker(new MarkerOptions()
                            .position(FLUX)
                            .title("Flux")
                            .snippet("Applied Physics and Electrical Engineering")
                            .alpha(-1f));
                    Flux.showInfoWindow();
                    Log.d(TAG, "This is " + Flux.getTitle());

                    GoogleMap.OnInfoWindowClickListener infoWindowClickListenerFlux = new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {

                            String title = marker.getTitle();
                            Log.d(TAG, "onInfoWindowClick: " + title);

                            if (title.equals(Flux.getTitle())) {
                                //Action for flux infowindow
                                Log.d(TAG, "This is flux infowindow " + Flux.getTitle());

                                //launches FluxActivity
                                startActivity(new Intent(GpsActivity.this, FluxActivity.class));

                            }
                        }

                    };
                    mMap.setOnInfoWindowClickListener(infoWindowClickListenerFlux);

                } else if (groundOverlay.getId().equals(vertigoOverlay.getId())) {
                    //Action for vertigo
                    Vertigo = mMap.addMarker(new MarkerOptions()
                            .position(VERTIGO)
                            .title("Vertigo")
                            .snippet("Architecture")
                            .alpha(-1f));
                    Vertigo.showInfoWindow();
                    Log.d(TAG, "This is " + Vertigo.getTitle());

                    GoogleMap.OnInfoWindowClickListener infoWindowClickListenerVertigo = new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            String title = marker.getTitle();
                            Log.d(TAG, "onInfoWindowClick: " + title);

                            if (title.equals(Vertigo.getTitle())) {
                                //Action for Vertigo infowindow
                                Log.d(TAG, "This is Vertigo infowindow " + Vertigo.getTitle());

                                //Launches VertigoActivity
                                startActivity(new Intent(GpsActivity.this, VertigoActivity.class));
                            }
                        }

                    };
                    mMap.setOnInfoWindowClickListener(infoWindowClickListenerVertigo);

                } else if (groundOverlay.getId().equals(auditoriumOverlay.getId())) {
                    //Action for auditorium
                    Auditorium = mMap.addMarker(new MarkerOptions()
                            .position(AUDITORIUM)
                            .title("Auditorium")
                            .snippet("Main Lecture Hall")
                            .alpha(-1f));

                    Auditorium.showInfoWindow();
                    Log.d(TAG, "This is " + Auditorium.getTitle());

                    GoogleMap.OnInfoWindowClickListener infoWindowClickListenerAuditorium = new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {

                            String title = marker.getTitle();
                            Log.d(TAG, "onInfoWindowClick: " + title);

                            if (title.equals(Auditorium.getTitle())) {
                                //Action for Auditorium infowindow
                                Log.d(TAG, "This is Auditorium infowindow " + Auditorium.getTitle());

                                //Launches AuditoriumActivity
                                startActivity(new Intent(GpsActivity.this, AuditoriumActivity.class));
                            }
                        }

                    };
                    mMap.setOnInfoWindowClickListener(infoWindowClickListenerAuditorium);

                } else if (groundOverlay.getId().equals(metaforumOverlay.getId())) {
                    //Action for metaforum
                    Metaforum = mMap.addMarker(new MarkerOptions()
                            .position(METAFORUM)
                            .title("Metaforum")
                            .snippet("Library")
                            .alpha(-1f));
                    Metaforum.showInfoWindow();
                    Log.d(TAG, "This is " + Metaforum.getTitle());

                    GoogleMap.OnInfoWindowClickListener infoWindowClickListenerMetaforum = new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            String title = marker.getTitle();
                            Log.d(TAG, "onInfoWindowClick: " + title);

                            if (title.equals(Metaforum.getTitle())) {
                                //Action for Metaforum infowindow
                                Log.d(TAG, "This is Metaforum infowindow " + Metaforum.getTitle());

                                //Launches MetaforumActivity
                                startActivity(new Intent(GpsActivity.this, MetaforumActivity.class));

                            }
                        }

                    };
                    mMap.setOnInfoWindowClickListener(infoWindowClickListenerMetaforum);

                } else if (groundOverlay.getId().equals(atlasOverlay.getId())) {
                    //Action for atlas
                    Atlas = mMap.addMarker(new MarkerOptions()
                            .position(ATLAS)
                            .title("Atlas")
                            .snippet("Industrial Design and Industrial Engineering")
                            .alpha(-1f));
                    Atlas.showInfoWindow();
                    Log.d(TAG, "This is " + Atlas.getTitle());

                    GoogleMap.OnInfoWindowClickListener infoWindowClickListenerAtlas = new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {

                            String title = marker.getTitle();
                            Log.d(TAG, "onInfoWindowClick: " + title);

                            if (title.equals(Atlas.getTitle())) {
                                //Action for Atlas infowindow
                                Log.d(TAG, "This is Atlas infowindow " + Atlas.getTitle());

                                //Launches AtlasActivity
                                startActivity(new Intent(GpsActivity.this, AtlasActivity.class));
                            }
                        }

                    };
                    mMap.setOnInfoWindowClickListener(infoWindowClickListenerAtlas);

                } else if (groundOverlay.getId().equals(carOverlay.getId())) {
                    //Action for car click
                    Log.d(TAG, "This is a car");
                    Flux = mMap.addMarker(new MarkerOptions()
                            .position(FLUX)
                            .title("Flux")
                            .snippet("Applied Physics and Electrical Engineering")
                            .alpha(0.1f));
                }
            }
        };
        mMap.setOnGroundOverlayClickListener(listener);
    }

    //Puts car somewhere on the map, to be later called when coordinates change.
    private void setupCarOverlay() {
        BitmapDrawable carBitmapDrawable=(BitmapDrawable)getResources().getDrawable(R.drawable.caricon);
        carBitmap =carBitmapDrawable.getBitmap();

        carOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .position(new LatLng(50.967455, 5.943757),4)
                .image(BitmapDescriptorFactory.fromBitmap(carBitmap))
                .zIndex(1)
                .bearing(315));
    }

    // Builds common notification settings; vibration pattern, title etc which is being sent from
    // the implementation.
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

    // Cancels and shows notifications changed accordingly after being built in buildCarNotifcation
    // depending upon the distance in a straight line from the last car location to the last user
    // location.
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
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(AUTONOMOUS_CAR_40M_NOTIFICATION));
                mNotificationManager.notify(AUTONOMOUS_CAR_40M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_40M_NOTIFICATION_ID] = true;
            }
        }
        if(deltaMeters >= 40 && deltaMeters <= 100 && !notificationArray[1]) {
            cancelNotification(AUTONOMOUS_CAR_40M_NOTIFICATION_ID);
            if(carNotificationConstant2==0){
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(AUTONOMOUS_CAR_100M_NOTIFICATION));
                mBuilder.setSound(null);
                mNotificationManager.notify(AUTONOMOUS_CAR_100M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_100M_NOTIFICATION_ID]=true;}
            else if(carNotificationConstant2==1){
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(AUTONOMOUS_CAR_100M_NOTIFICATION));
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

    // Cancels any notification. The id is the id given to the notifcation when created. the
    // notifcationarray with the element of this ID is set to false and the notifcation itself is
    // cancelled via notifcationmanager.
    public void cancelNotification(int id) {
        if(notificationArray[id]){
            mNotificationManager.cancel(id);
            notificationArray[id]=false;}
    }

    // Creates appropriate JSON objects to be called for creating content instances and containers
    // on oneM2M. These are global variables.
    private void createVRUJSONS() {
        VRUgps = new OneM2MMqttJson(oneM2MVRUAeRi,oneM2MVRUAePass,oneM2MVRUAeRn,userName);

        try {
            contentCreateGPS = VRUgps.CreateContentInstanceGps(null,null,
                    null,null);
            contentCreateUserStatus = VRUgps.CreateContentInstanceStatus(null,
                    null,0);
            String to = "/server/server/aeSmartCampus1/Users/" + userName + "/Status";
            contentCreateUserStatus.getJSONObject("m2m:rqp").put("to",to);
            contentCreateCallCar = VRUgps.CreateContentInstanceCallTaxi(null,null,
                    0,userName);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Creates broadcast receiver for DetectedActivitiesIntentService, this receives all activites
    // with their confidence. The Highest confidence activity is then sent to checkActivityType and
    // published to oneM2M and Huawei and logged.
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
                            publishUserStatus(userActivityType,mActivityRecognitionTimestamp,
                                    userConfidence);
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
    }

    // Checks which activity type is detected and sends back appropriate string to be put into json
    // for status detection.
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

    // Starts the tracking of UserActivity via BackgroundDetectedAcitiviesService, which sends
    // activities list to DetectedActivitiesIntentService, this gets the highest confidense activity
    // type and broadcasts to setBroadcastReceiver
    private void startTrackingUserActivity(){
        Intent intent1 = new Intent(GpsActivity.this, BackgroundDetectedActivitiesService.class);
        Log.d(TAG, "startTrackingUserActivity: ");
        startService(intent1);
    }

    // Builds the OneM2M broker connection, subscribes to the VRU ae Response topic and creates
    // UserID container.
    private MqttAndroidClient buildOneM2MVRU(MqttAndroidClient mMqttAndroidClient,
                                             String userId1) {
        userId1 = userId1.replace("s","suser");
        String mqttBrokerUrl = "tcp://vmi137365.contaboserver.net:1883";
        mMqttAndroidClient = getMqttClient(getApplicationContext(), mqttBrokerUrl, userName);
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
    }

    // Subscribes to response topic on OneM2M the listener created here get called whenever a
    // message on a subscribed topic on OneM2M arrives.
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
    }

    // Processes the messages that are incoming from oneM2M from a certain subscribed topic as
    // defined in subscribeToTopic. This is the handler for incoming car messages and if this is
    // from the RTK (accurate GPS) from the car or from the flowradar. Currently only RTK is used.
    private void oneM2MMessagesHandler(String topic, MqttMessage message, Long timeUnix,
                                       String lastTimeUTC) throws JSONException {
        JSONObject messageCar = new JSONObject(new String(message.getPayload()));
        Log.d(TAG, "oneM2MMessagesHandler: Arrived");

        if(topic.equals(CsmartCampusCarsSubscriptionTopic)){
            Log.d(TAG, "oneM2MMessagesHandler: SubCar");

            String comparator = messageCar.getJSONObject("m2m:rqp").getJSONObject("pc")
                    .getJSONArray("m2m:sgn").getJSONObject(0).getString("sur");
            String contentCarString = messageCar.getJSONObject("m2m:rqp").getJSONObject("pc")
                    .getJSONArray("m2m:sgn").getJSONObject(0).getJSONObject("nev")
                    .getJSONObject("rep").getJSONObject("m2m:cin").getString("con");

            Long dataGenerationTimestamp=null;
            boolean newData=false;
            Log.d(TAG, "oneM2MMessagesHandler: " + comparator);

           /* if(comparator.equals("/server/server/aeTechnolution/flowradar/flowradar_car/subFlowradar_car")){
                Log.d(TAG,"oneM2MMessages + " + contentCarString);
                String[] separated = contentCarString.split(",");
                lastFlowRadar = System.currentTimeMillis();

                String longitudeCarString = separated[3];
                dataGenerationTimestamp = separated[2].split(":")[1];
                String[] carLonseparated = longitudeCarString.split(":");
                carLon = Double.parseDouble(carLonseparated[1]);
                String latitudeCar = separated[4];
                carHeading = Float.parseFloat((separated[6].split(":"))[1].replace("}",""));
                carSpeed = Float.parseFloat(separated[5].split(":")[1]);
                String[] carLatseparated = latitudeCar.split(":");
                carLat = Double.parseDouble(carLatseparated[1]);
                newData = true;}*/
            if(comparator.equals("/server/server/aeTechnolution/prius/GPS/subPrius")){
                noRTK=false;
                Log.d(TAG, "oneM2MMessagesHandler: RTK");
                lastRTK = System.currentTimeMillis();
                String[] separated = contentCarString.split(",");
                String longitudeCarString = separated[1];
                dataGenerationTimestamp = Long.parseLong(separated[2].split(":")[1].
                        replace(" ",""));
                Log.d(TAG, "oneM2MMessagesHandler: " +dataGenerationTimestamp);
                String[] carLonseparated = longitudeCarString.split(":");
                carLon = Double.parseDouble(carLonseparated[1]);
                carSpeed = Float.parseFloat(separated[5].split(":")[1]);
                String latitudeCar = separated[3];
                carHeading = Float.parseFloat((separated[6].split(":"))[1]);
                String[] carLatseparated = latitudeCar.split(":");
                carLat = Double.parseDouble(carLatseparated[1]);
                newData = true;
            }

            if(newData) {
                carLoc = new LatLng(carLat, carLon);

                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "oneM2MMessagesHandler: Updating car");
                        carOverlay.setPosition(carLoc);
                        if(carHeading<0){
                            carHeading = 360 + carHeading;
                        }
                        carOverlay.setBearing(carHeading);
                    } // This is your code
                };
                mainHandler.post(myRunnable);

                if (mCurrentlocation != null) {
                    Double deltaMeters;
                    deltaMeters = DifferenceInMeters(carLat, carLon, mCurrentlocation.getLatitude(),
                            mCurrentlocation.getLongitude());
                    Log.d(TAG, "Deltameter:" + deltaMeters);
                    handleCarNotification(deltaMeters);
                }

                pilotLogging(LOGGING_VEHICLE, dataGenerationTimestamp, contentCarString);
            }
        }

        else if(topic.equals(CsmartcampusSubscriptionTopic)){
            if(messageCar.getJSONObject("m2m:rsp").getString("rqi").equals(userName)){
                String contentTimeString = messageCar.getJSONObject("m2m:rsp").getJSONObject("pc")
                        .getJSONArray("m2m:cin").getJSONObject(0)
                        .getString("rn");
                Long timeGps = Long.parseLong(contentTimeString);
                Long deltaTime = timeUnix - timeGps;
                String latencyFromGPSTillReceive = Long.toString(deltaTime);
                Log.d(TAG, "Latency:" + latencyFromGPSTillReceive);
            }
        }
    }

    // Publishes any message to to a topic on oneM2M defined by the input and logs this message.
    // This is used for publishing status, GPS and call taxi messages. Messagetype defines what kind
    // of messsage this is and if it needs to be logged or not.
    public void publishAndLogMessage(@NonNull MqttAndroidClient client, @NonNull final String msg,
                                     int qos, @NonNull final String topic, final int messageType,
                                     final String logmsg, @NonNull final Long generationTimeStamp)
            throws MqttException, UnsupportedEncodingException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(5866);
        message.setRetained(true);
        message.setQos(qos);
        Log.d(TAG, "Sent message: " + new String(message.getPayload()));
        client.publish(topic, message).setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "onSuccess message: ");
                if(messageType!=LOGGING_NOTNEEDED){
                    if(loggingSwitch.isChecked() && !runNumberText.getText().toString().isEmpty()
                            && !experimentNumberText.getText().toString().isEmpty()){
                pilotLogging(messageType, generationTimeStamp, logmsg);}}
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                packetLosses++;
            }
        });
    }

    // Function that processes al logging depending on the messagetype (what kind of log), the data
    // and the generation timestamp that is needed to be put into the log.
    private void pilotLogging(int messageType, long generationTimeStamp, String data) {
        String log;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyyMMdd");

        if(data!=null){
            //deletes some rubbish from the made messages
        data = data.replace("\\", "");
        data = data.replace(" ", "");}

        //Adds a 0  to the experiment and run number if it is below 10 like 04 and not 4.
        String experimentNumberString = (Integer.parseInt(experimentNumberText.getText().toString())
                < 10 ? "0" : "") + Integer.parseInt(experimentNumberText.getText().toString());
        String runNumberString = (Integer.parseInt(runNumberText.getText().toString())
                < 10 ? "0" : "") + Integer.parseInt(runNumberText.getText().toString());


        switch(messageType) {
            // Checks what message needs to be logged and makes a logging entry accordingly. Also
            // makes a new log file if not existing yet and adds it to the vector pointing to all
            // log files for uploading to firebase.
            case LOGGING_NOTNEEDED:
                break;

            case LOGGING_GPS:
                Log.d(TAG, "pilotLogging: GPS");
                log = ",1," + userName + "," + "SENT,CELLULAR,AutoPilot.SmartphoneGPS,"
                        + UUID.randomUUID().toString() + "," + userName + "," + data ;

                String fileNameGPS = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_1.csv";

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_1.csv",log);

                if(fileNameVector==null){fileNameVector.add(fileNameGPS);}
                if(!fileNameVector.contains(fileNameGPS) && fileNameVector !=null){fileNameVector.
                        add(fileNameGPS);}

                break;

            case LOGGING_STATUS:
                Log.d(TAG, "pilotLogging: Status");
                log = ",2," + userName + "," + "SENT,CELLULAR,AutoPilot.SmartphoneUserActivity,"
                        + UUID.randomUUID().toString() + "," + userName + ", " + data;

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_2.csv",log);

                String fileNameStatus = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_2.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameStatus);}
                if(!fileNameVector.contains(fileNameStatus)){fileNameVector.add(fileNameStatus);}
                break;

            case LOGGING_VEHICLE:
                Log.d(TAG, "pilotLogging: Vehicle");
                log = ",3," + userName + "," + "RECEIVED,CELLULAR,AutoPilot.PriusStatus," + "112233"
                        + generationTimeStamp + ",112233," + data;

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        +userName + "_3.csv",log);

                String fileNameCar = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+"_3.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameCar);}
                if(!fileNameVector.contains(fileNameCar)){fileNameVector.add(fileNameCar);}

                break;
            case LOGGING_HUAWEI_RECEIVED:
                Log.d(TAG, "pilotLogging: HuaweiReceived");

                log = ",4," + userName + "," + "RECEIVED,CELLULAR,AutoPilot." +
                        "HuaweiGeofencingRectangle," + " ,3172," + data;

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName + "_4.csv",log);

                String fileNameHR = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName +"_4.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameHR);}
                if(!fileNameVector.contains(fileNameHR)){fileNameVector.add(fileNameHR);}
                break;

            case LOGGING_HUAWEI_SENT:
                Log.d(TAG, "pilotLogging: HuaweiSent");
                log = ",5," + userName + "," + "SENT,CELLULAR,AutoPilot.HuaweiGeofencingGPS," +
                        UUID.randomUUID().toString() + ','
                        + userName + "," + data;

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+ "_5.csv",log);

                String fileNameHS = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+"_5.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameHS);}
                if(!fileNameVector.contains(fileNameHS)){fileNameVector.add(fileNameHS);}
        }
    }

    // Writes a logfile or appends this file if it is already existing with an arbitrary array (does
    // not matter how large) seperated by commas. (Time,Latency, ..... Lat, Lon) will be one line in
    // an CSV file to excel.
    private void writeToLogFile(String Filename , String entry) {
       /* String FILENAME = userId + "-" + "OneM2MBackAndForthLatency.csv";
        StringBuilder stringBuilder = new StringBuilder();

        //Array loops all sring entries and seperates by comma as in CSV file
        for(String string : entry){
            String strTemp = string + ',';
            stringBuilder.append(strTemp);
        }*/
        String entryFile = entry + "\n";
        try {
            FileOutputStream out = openFileOutput(Filename,Context.MODE_APPEND);
            out.write((String.valueOf(System.currentTimeMillis())+ entryFile).getBytes());
            Log.d(TAG, "write to log");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG,"writeToLogFile" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "writeToLogFile" + e.toString());
        }
    }

    // Initializes MQTT client
    public MqttAndroidClient getMqttClient(@NonNull Context context,@NonNull String brokerUrl,
                                           @NonNull String clientId) {
        final MqttAndroidClient mqttClient = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = mqttClient.connect(setMqttConnectionOptions());
            if(token==null){Log.d(TAG, "token is null");}
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "getMqttClient: Success");
                    OneM2MMqttJson VRU = new OneM2MMqttJson(oneM2MVRUAeRi, oneM2MVRUAePass,
                            oneM2MVRUAeRn,userName);
                    subscribeToTopic(CsmartcampusSubscriptionTopic);
                    subscribeToTopic(CsmartCampusCarsSubscriptionTopic);
                    try {
                        publishAndLogMessage(onem2m,VRU.
                                CreateContainer(userName).toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null);
                        publishAndLogMessage(onem2m,VRU.
                                CreateUserContainer("Gps").toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null);
                        publishAndLogMessage(onem2m,VRU.
                                CreateUserContainer("Status").toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null);
                        publishAndLogMessage(onem2m,VRU.
                                CreateUserContainer("CallTaxi").toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null);
                        publishAndLogMessage(onem2m,VRU.
                                CreateTaxiSubContainer().toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null);
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
    }

    //Sets options for the MQTT client (Clean session, automatic reconnect etc). Sends this back to
    // where it is called.
    private MqttConnectOptions setMqttConnectionOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName("autopilot");
        mqttConnectOptions.setPassword("onem2m".toCharArray());
        mqttConnectOptions.setKeepAliveInterval(60);
        return mqttConnectOptions;
    }

    // Function called when asynctasks are finished. In this case handles the response received by
    // the OkHTTP Huawei connection asynctask. Whenever Huawei sends something back (whenever we
    // send them something by post) this function is called. This then handles the rectangle
    // placement and speed indication in the google maps layout and also handles logging of Huawei
    // messaging as well as notifications for the rectangle.
    @Override
    public void processFinish(Bundle output) {
        if(output!=null && output.getString("error")==null) {
            Log.d(TAG, "processFinish: " + output.getString("returnMessage")
                    + "," + output.getBoolean("isInRectangle"));

            if(loggingSwitch.isChecked() && !runNumberText.getText().toString().isEmpty()
                    && !experimentNumberText.getText().toString().isEmpty()) {
                pilotLogging(LOGGING_HUAWEI_RECEIVED, 150000000, output.
                        getString("returnMessage"));
            }

            if (output.getBoolean("isInRectangle")) {
                handleCarNotificationHuawei(true);
            }

            if (output.getBoolean("isInRectangle")) {
                handleCarNotificationHuawei(false);
            }

            LatLng[] points = new LatLng[5];
            double[] rectangleLat = output.getDoubleArray("rectangleLat");
            double[] rectangleLon = output.getDoubleArray("rectangleLon");
            for(int  i=0 ; i<rectangleLat.length;i++){
                points[i] = new LatLng(rectangleLat[i],rectangleLon[i]);
            }

            //Function that use the points in the rectangle for visualization of position and speed
            speedPolygon(points);
            geoFencingCarPolygon(points);
        }
        if(output !=null && output.getString("error")!=null) {
            Log.d(TAG, "processFinish: " + output.getString("error"));
            handleCarNotificationHuawei(false);
        }
        handleCarNotificationHuawei(false);
    }

    // Makes a tansformation of the speed into distance between points of the existing Huawei
    // rectangle, then fills this up depending on speed with a certain color. Takes the points of
    // the huawei rectangle.
    private void speedPolygon(LatLng[] points) {
        LatLng[] pointsSpeed = new LatLng[4];
        pointsSpeed[0] = points[0];
        pointsSpeed[1] = points[3];
        if(carSpeed!=null){

           //Calculates the differences in lat,lon in meters, but scaled at 100 km/h (27.8 m/s), to visualise speed
           Double DeltaLat = ((-81.5 * Math.cos(carHeading*Math.PI/180))/27.8) * carSpeed;
           Double Deltalong = ((-81.5 * Math.sin(carHeading*Math.PI/180))/27.8) * carSpeed;

           //point for array element 2
           Double lat2_2 = points[3].latitude - (DeltaLat*360/40075000);
           Double lon2_2 = points[3].longitude - (Deltalong*360/40075000);
           pointsSpeed[2] = new LatLng(lat2_2,lon2_2);


           //point for array element 3
           Double lat2_3 = points[0].latitude - (DeltaLat*360/40075000);
           Double lon2_3 = points[0].longitude - (Deltalong*360/40075000);
           pointsSpeed[3] = new LatLng(lat2_3,lon2_3);


           //fills up the speedpolygon with different colours based on its speed
           if (carSpeed < 5){

               if(speedPolygon==null){
                   speedPolygon= mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.WHITE)
                           .fillColor(Color.WHITE));
               }

               //* else {
               if(speedPolygon!=null){
                   speedPolygon.remove();
                   speedPolygon = mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.WHITE)
                           .fillColor(Color.WHITE));

               }

           }

           else if (carSpeed >= 5 && carSpeed <= 10){

               if(speedPolygon==null){
                   speedPolygon= mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.LTGRAY)
                           .fillColor(Color.LTGRAY));
               }

               //* else {
               if(speedPolygon!=null){
                   speedPolygon.remove();
                   speedPolygon = mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.LTGRAY)
                           .fillColor(Color.LTGRAY));

               }
           }

           else if (carSpeed > 10 && carSpeed <= 15){

               if(speedPolygon==null){
                   speedPolygon= mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.GRAY)
                           .fillColor(Color.GRAY));
               }

               //* else {
               if(speedPolygon!=null){
                   speedPolygon.remove();
                   speedPolygon = mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.GRAY)
                           .fillColor(Color.GRAY));

               }
           }

           else if (carSpeed > 15 && carSpeed <= 20){

               if(speedPolygon==null){
                   speedPolygon= mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.DKGRAY)
                           .fillColor(Color.DKGRAY));
               }

               //* else {
               if(speedPolygon!=null){
                   speedPolygon.remove();
                   speedPolygon = mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.DKGRAY)
                           .fillColor(Color.DKGRAY));

               }
           }

           else if (carSpeed > 20 && carSpeed <= 25){

               if(speedPolygon==null){
                   speedPolygon= mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.BLACK)
                           .fillColor(Color.BLACK));
               }

               //* else {
               if(speedPolygon!=null){
                   speedPolygon.remove();
                   speedPolygon = mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.BLACK)
                           .fillColor(Color.BLACK));

               }
           }

           else if (carSpeed > 25 && carSpeed <= 28){

               if(speedPolygon==null){
                   speedPolygon= mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.RED)
                           .fillColor(Color.RED));
               }

               //* else {
               if(speedPolygon!=null){
                   speedPolygon.remove();
                   speedPolygon = mMap.addPolygon(new PolygonOptions()
                           .add(pointsSpeed)
                           .zIndex(0)
                           .strokeColor(Color.RED)
                           .fillColor(Color.RED));

               }
           }




            }
        }

    // Makes a notification whenever user is in the rectangle of the Huawei geofencing rectangle.
    // inZone is boolean whether the user is in the zone or not.
    private void handleCarNotificationHuawei(boolean inZone) {
        if(inZone){
            long[] vibrationPattern = {Long.valueOf(0),Long.valueOf(500)};
            if(!notificationArray[HUAWEI_NOTIFICATION_ID]){
            String notificationText = "You are in the rectangle";
            mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentTitle("Autonomous car warning")
                .setAutoCancel(false)
                .setVibrate(vibrationPattern);
        mNotificationManager.notify(HUAWEI_NOTIFICATION_ID, mBuilder.build());
        notificationArray[HUAWEI_NOTIFICATION_ID] = true;}
        if(!inZone){
            cancelNotification(HUAWEI_NOTIFICATION_ID);
            Log.d(TAG, "handleCarNotificationHuawei: Inzone false");
        }
    }}

    // Executes an OkHTTPpost asynctask to send a json to a certain URL that is indicated by the url
    // and json. results of this is handled in process finish (used for Huawei communication).
    void okHTTPPost(String url, String json) {
        okHttpPost post1 = new okHttpPost(this);
        String[] string = new String[3];
        string[0]=url;
        string[1]=json;
        string[2]=userName;
        post1.execute(string);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(fileNameVector!=null){uploadLogFilesFirebase();}
        super.onStop();
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

    // Main function, is called whenever a new location is found in onlocationresult. Functions
    // sends last location to communication function publishGpsData and updated textview fields in
    // activity. timestampUTC is GPS timestamp from last location result.
    private void updateLocationUI(Long timestampUTC) {
        if(!gpsHolder.isChecked()){
            isAlreadyHeld=false;
        }

        if(mCurrentlocation!=null){
            if(!checkHoldGPSLocation()){
                Longitude = mCurrentlocation.getLongitude();
                Latitude = mCurrentlocation.getLatitude();
                Accuracy = mCurrentlocation.getAccuracy();
                Log.d(TAG, "updateLocationUI: normal operation");
            }

            //Speed implementation
            String speedGPS;
            String[] speedGPSandBearing = calculateSpeedAndBearing(Latitude,
                    Longitude,timestampUTC);
            if(mCurrentlocation.hasSpeed()){speedGPS=Float.toString(mCurrentlocation.getSpeed());
            }
            else{
                speedGPS = speedGPSandBearing[0];}
            if(mCurrentlocation.hasBearing()){bearing = Float.toString(mCurrentlocation.
                    getBearing());}
            else{
                bearing = speedGPSandBearing[1];}

            //Sending GPS to oneM2M
            try {
                if (speedGPS == null) {speedGPS = "0.0";
                }
                if(mCurrentlocation!=null){
                publishGpsData(Latitude,Longitude,Accuracy, mCurrentlocation.getTime(),
                        speedGPS,bearing);}
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Old clock for car notifications, now performed whenever a message arrives.
            /*Double deltaMeters;
            deltaMeters = DifferenceInMeters(carLat,carLon,mCurrentlocation.getLatitude(),
            mCurrentlocation.getLongitude());
            handleCarNotification(deltaMeters);*/

            // Sets the textviews to the lastest location values in the activity
            String Bearing = Double.toString(mCurrentlocation.getBearing());
            latitude = String.format(Locale.ENGLISH,"%f", mCurrentlocation.getLatitude());
            longitude = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getLongitude());
            longitude = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getLongitude());
            bearing = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getBearing());
            if(Build.VERSION.SDK_INT>=26){
                bearingAccuracy = String.format(Locale.ENGLISH, "%f", mCurrentlocation.
                        getBearingAccuracyDegrees());}
            speed = String.format(Locale.ENGLISH, "%f", mCurrentlocation.getSpeed());
            viewLatitude.setText(latitude);
            viewLongitude.setText(longitude);
            viewBearing.setText(bearing);
            viewSpeed.setText(speedGPS);
            //viewBearingAccuracy.setText(bearingAccuracy);

            //Data to Firebase, not needed atm
            /*mDatabase.child("users").child(userId).child("longitude").setValue(longitude);
            mDatabase.child("users").child(userId).child("latitude").setValue(latitude);
            mDatabase.child("users").child(userId).child("speed").setValue(speed);*/
        }
    }

    // Used to check if holdGPS is checked, if it is then last location when checked is only send
    //(Last location is maintained), when it is unchecked, locations are updated normally.
    private boolean checkHoldGPSLocation() {
        if(gpsHolder.isChecked()) {
            if(!isAlreadyHeld){
                Longitude = mCurrentlocation.getLongitude();
                Latitude = mCurrentlocation.getLatitude();
                Accuracy = mCurrentlocation.getAccuracy();
                isAlreadyHeld=true;
                Log.d(TAG, "updateLocationUI: !isAlreadyHeld");
                return true;
            }
            return true;
        }
        return false;
    }

    // Function for creating the final Json to be sent to the publishandlogmessage function for
    // sending and logging of the userstatus to oneM2M. The input concerns the confidence activity
    // type and timestamp of the received detected user activity from the AcitivtyRecognition
    // broadcast receiver.
    private void publishUserStatus(String activity, Long timeStamp, int confidence)
            throws JSONException, MqttException, UnsupportedEncodingException {
        String con = "{\"activity\":" + "\"" + activity + "\"" + "," +  "\"activity confidence\":"
                + confidence + ",\"timestampUtc\":" + timeStamp + "}";
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin")
                .put("con", con);
        String to = "/server/server/aeSmartCampus1/Users/" + userName + "/Status";
        contentCreateUserStatus.getJSONObject("m2m:rqp").put("to",to);
        String contentCreateStatus = contentCreateUserStatus.toString();
        String logmessage = contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc")
                .getJSONObject("m2m:cin").getString("con");
        publishAndLogMessage(onem2m,contentCreateStatus,0,oneM2MVRUReqTopic
                ,LOGGING_STATUS,logmessage, timeStamp);} //

    // Function for creating the final json to be sent to the publishandlogmessage function for
    // sending and logging of the GPS information to OneM2M. The inputs concern the found lat,lon
    // speed etc from last found location of the Google mfusedlocationclient.
    private void publishGpsData(Double latitude, Double longitude, Float Accuracy,
                                Long formattedDate, String speedGPS, String manualBearing)
            throws JSONException, MqttException, UnsupportedEncodingException {

        String formattedDateString = "UTC"+ Long.toString(formattedDate);
        UTCPacketLossCheck = formattedDate.toString();
        String topic = "/server/server/" + "aeSmartCampus1" + "/Users/" + userName + "/gps";
        String con = "{\"type\":5,\"id\":"+userName + ",\"timestampUtc\":" + formattedDate +
                ",\"lon\":" + longitude + ",\"lat\":"+ latitude + ",\"speed\":"+ speedGPS +
                ",\"heading\":"+manualBearing+ ",\"accuracy\":"+Accuracy+ "}";
        String conHuawei = "{\"type\":5,\"id\":" + userName + ",\"timestampUtc\":" +
                formattedDateString + ",\"lon\":" + longitude + ",\"lat\":"+ latitude +
                ",\"speed\":"+ speedGPS + ",\"heading\":"+manualBearing+ ",\"accuracy\":"+
                Accuracy+ "}";
        Log.d(TAG, "publishGpsData: " + conHuawei);
        okHTTPPost(huaweiUrl,conHuawei);
        contentCreateGPS.getJSONObject("m2m:rqp").put("to",topic);
        contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin")
                .put("con", con);
        contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin")
                .put("rn", formattedDate);
        String contentCreate = contentCreateGPS.toString();
        String logmessage = contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc")
                .getJSONObject("m2m:cin").getString("con");

        publishAndLogMessage(onem2m,contentCreate,0,oneM2MVRUReqTopic,LOGGING_GPS,
                logmessage,formattedDate);
        if(loggingSwitch.isChecked() && !runNumberText.getText().toString().isEmpty()
                && !experimentNumberText.getText().toString().isEmpty()){
           pilotLogging(LOGGING_HUAWEI_SENT,formattedDate,conHuawei);
        }
    }

    // Calculates the manual speed and bearing if google does not provide any (when inside for
    // example). Uses last location and new location for the calculation of this speed and bearing.
    private String[] calculateSpeedAndBearing
            (Double latitude, Double longitude, Long timeStamp){
        String speedGPS;
        String[] speedandBearing = new String[2];

        if(lastLat == 0.00){
            lastLat=latitude;
            lastLon=longitude;
            lastTime = timeStamp;
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
    } //

    private double DifferenceUTCtoSeconds(Long timeStamp, Long timeStamp2){
        return timeStamp-timeStamp2;
    }

    // Difference in meters (birds flight) using 'haversine' formula, gives back distance between
    // two points in doubles. the latitude and longitude are in degrees.
    private double DifferenceInMeters(Double lastLat,Double lastLon,Double lat,Double lon){
        Double deltaPhiLon = (lon - lastLon)*Math.PI/180;
        Double deltaPhilat = (lat - lastLat)*Math.PI/180;
        lastLat = lastLat*Math.PI/180;
        lat = lat*Math.PI/180;

        Double earth = 6371e3;

        Double a  = Math.sin(deltaPhilat/2)*Math.sin(deltaPhilat/2)+Math.cos(lastLat)*Math.cos(lat)*Math.sin(deltaPhiLon)*Math.sin(deltaPhiLon);
        Double c  = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        Double d = earth*c;
        return d;
    }

    // Calculates the bearing or direction of the user based upon two location points (the last one
    // and the latest), sends this back to be used in the logging as well as in layout. Only used
    // when Google does not give any heading/bearing, hence the name manual. lat and lon in degrees.
    private double ManualBearing(Double lastLat,Double lastLon,Double lat,Double lon){
        Double deltaPhiLon = (lon - lastLon) * Math.PI/180;
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

    @Override
    protected void onDestroy() {
        Log.d(TAG, "destroy");

        if(fileNameVector!=null){uploadLogFilesFirebase();}
        FirebaseAuth.getInstance().signOut();

        stopTracking();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        mNotificationManager.cancelAll();

        super.onDestroy();
    }

    // Removes last location of Huawei geofencing rectangle and adds the new location of the
    // rectangle in the map, this function accepts array of LatLng points.
    private void geoFencingCarPolygon(LatLng[] points){
        if(geoFencingPolygon==null){
            geoFencingPolygon = mMap.addPolygon(new PolygonOptions()
                    .add(points)
                    .zIndex(0)
                    .strokeColor(Color.LTGRAY));
        }
        else{
            geoFencingPolygon.remove();
            geoFencingPolygon = mMap.addPolygon(new PolygonOptions()
                    .add(points)
                    .zIndex(0)
                    .strokeColor(Color.LTGRAY));
        }
    }

    // Uploads the log files storedto firebase by using a vector as dataformat (an dynamically
    // expandable array), currently called whenever the app is paused or destroyed.
    private void uploadLogFilesFirebase() {
        fileNameVector.elements();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        for(Enumeration<String> e = fileNameVector.elements(); e.hasMoreElements();){
            String filename = e.nextElement();
        String filePath = getApplicationContext().getFilesDir() + "/" + filename;

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
            }
        });}
    }

    // Constructor of locationrequest, sets certain settings of the locationrequest (interval,
    // priority etc) for the client that sends the location updates.
    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INVTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    //Builds the location request upon the settings build in createLocationRequest
    private void buildLocationSettingsRequest(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(this.mLocationRequest);
        mLocationSettingsRequest= builder.build();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userId",userName);
        outState.putString("password",password);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    // Override function for giving functionality to whenever the back button is pressed
    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        stopTracking();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        this.finishAffinity();
    }

    // Starts the actual location updates from the settings that have been built before. Sets on
    // success listeners and on failure listeners to inform whether the locationupdates request was
    // successful.
    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>()
                {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(GpsActivity.this
                                        .mLocationRequest, mLocationCallback, Looper.myLooper());
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
                                    rae.startResolutionForResult(GpsActivity.this,
                                            REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie){

                                }
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate," +
                                        " and cannot be fixed. Please fix in settings";
                                Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_LONG).show();
                        }}
                });
    }

    //Requests the user for permission of GPS, a rationale is given if needed to state the reason
    // why GPS is needed. When permissions are requested and accepted or cancelled, the override
    //  function onRequestPermissionsResult is called.
    private void requestPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale){
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "We need your gps data for essential app functions and research",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(GpsActivity.this,new String[]{Manifest
                                    .permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSIONS_REQUEST_CODE);

                }});
            int snackbarTextId= android.support.design.R.id.snackbar_text;
            TextView textView =(TextView) snackbar.getView().findViewById(snackbarTextId);
            textView.setTextColor(Color.WHITE);
            snackbar.getView().setBackground(permissionGrantedSnackbarShape);
            snackbar.show();
        }
            ActivityCompat.requestPermissions(GpsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    // Handler for permission results (if cancelled or accepted), this is called whenever a
    // permission is given for something (@Override function). This then starts location updates,
    // gives feedback that user has indeed given permissions to start GPS updates.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
                snackbar.getView().setBackground(permissionGrantedSnackbarShape);
                snackbar.show();
                startLocationUpdates();
            }else{
            }
        }
    }

    // Checks whether user has already given permission to use GPS, gives back a boolean (true or
    // false) that is used to either request permissions for GPS or start location updates.
    private boolean checkPermissions() {
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;

    }

    // Checks if user is still logged in via Firebase if not, logs in with stored credentials from
    // Firebase, if this is null uses stored credentials from User class and if this is null goes
    // back to login screen. FirebaseUser user is sent since this variable changes. Void function.
    private void userCheck(FirebaseUser user) {
        if (user == null) {
            MainActivity Login =  new MainActivity();
            if(userName!=null){
            Login.login(userName+"@random.com",password);
            Log.d(TAG, "userCheck: User is null");}
            else if(userNameStored!=null){
                Login.login(userNameStored,passwordStored);
            }
            else{
            Intent loginIntent = new Intent(GpsActivity.this, MainActivity.class);
            startActivity(loginIntent);}
        }
    }

    // Gets the username from Firebase
    private void getUsername(FirebaseUser user) {
        String userEmail = user.getEmail();
        userName = userEmail.replace("@random.com","");
    }


    private void createLocationCallback() {
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Long timestamp = System.currentTimeMillis();
                mCurrentlocation= locationResult.getLastLocation();
                updateLocationUI(timestamp);
            }
        };
    } //This is called whenever a new location is found,
    // this is the clock where updatelocationUI runs on.

    // Updates values from savedinstance, for example if user pauses app and comes back, locations
    // are pulled from the last savedInstanceState
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentlocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                String mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
        }
    }

    // Stops the detection of DetectedUserActivity (Called in OnDestroy)
    private void stopTracking() {
        Intent intent = new Intent(GpsActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    // Sends a message to OneM2M CallCar container when button is clicked
    // for calling a taxi. This message is then forwarded to Csmartcampus topic for IBM rebalancing
    // service via the subscription container CallTaxi_sub
    public void CallCar() {
        try {
            publishAndLogMessage(onem2m,VRUgps.CreateContentInstanceCallTaxi(mCurrentlocation.getLatitude(), mCurrentlocation.getLongitude(), System.currentTimeMillis(), userName).toString(),0,oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null, null);
        } catch (JSONException e) {
            Log.d(TAG, "CallCar: "+e.toString());
        } catch (UnsupportedEncodingException e){
            Log.d(TAG, "CallCar: "+e.toString());
        } catch (MqttException e) {
            Log.d(TAG, "CallCar: "+e.toString());
        }
    }
    //TODO Extrapolating the vehicle speed heading if time is too long/delay
}
