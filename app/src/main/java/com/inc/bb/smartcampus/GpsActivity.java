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
import android.net.Uri;
import android.os.Bundle;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

;


public class GpsActivity extends AppCompatActivity implements MapViewConstants, OnMapReadyCallback {
    String TAG = "GpsActivity";
    private Drawable permissionGrantedSnackbarShape;

    //Maps
    private GoogleMap mMap;

    //Motionplanning and calltaxi
    Polyline polylineMP = null;
    boolean motiongPlanningResponseReceived;
    boolean taxiNotificationNeeded = true;
    android.app.Fragment campusCar;

    //heatmaps
    HeatmapTileProvider mProvider;

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

    //Initial values for speed and bearing calculations
    Double lastLat = 0.00;
    Double lastLon = 0.00;
    Long lastTime;

    //Service broadcast receivers
    BroadcastReceiver broadcastReceiver, broadcastReceiverCarRTK;
    BroadcastReceiver broadcastReceiverLayoutChecker;
    BroadcastReceiver broadcastReceiverCarDataHuawei;
    BroadcastReceiver broadcastReceiverMotionplanningPath;
    BroadcastReceiver broadcastReceiverVRUData;

    //Notification global variables
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    int carNotificationConstant = 0;
    int carNotificationConstant2 = 0;
    double carLon = 5.623863;
    double carLat = 51.475792;
    Float carHeading = null;
    Float carSpeed = null;

    //Logging files
    File file;
    int experimentNumber = 0;
    int runNumber = 0;

    //Logging layout widgets
    EditText runNumberText, experimentNumberText;
    Switch loggingSwitch;

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
    private final static int TAXI_COMING_NOTIFICATION_ID = 4;
    Boolean[] notificationArray = new Boolean[10];
    LatLng carLoc;

    //Car marker
    GroundOverlay carOverlay;
    Bitmap carBitmap;
    com.google.android.gms.maps.model.Polygon geoFencingPolygon;
    com.google.android.gms.maps.model.Polygon speedPolygon;

    //userId and Password from Firebase as well as the stored versions.
    public String userName;
    String password;
    String passwordStored;
    String userNameStored;

    public String longitude;
    public String latitude;
    public String bearing;
    public String speed;

    //Cockpit VRU Data variables
    List<Circle> VRUCircleList = new ArrayList<Circle>();
    Vector<String> VRUIdVector = new Vector<>();
    boolean TAXIVruCircle = false;
    Circle taxiCALLcircle;

    //Request code for the permissions intent (asking for some permission)
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        //Sets orientation so the screen is locked to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Assigning of notification sound from downloaded google translate sound and filling of
        //notification array (fills up if notifications are active).
        AUTONOMOUS_CAR_25M_NOTIFICATION_SOUND = Uri.parse("android.resource://" + getPackageName() +
                "/" + R.raw.translate_tts);
        Arrays.fill(notificationArray, false);

        //Get data from intent from MainActivity
        password = getIntent().getStringExtra("password");
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

        //created the broadcast receivers for all services


        //Notification builder
        //buildCarNotification(AUTONOMOUS_CAR_NOTIFICATION_TITLE);
        permissionGrantedSnackbarShape = ContextCompat.getDrawable(getApplicationContext(), R.drawable.snackbarshape);

        //Firebase initialization
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        // Huawei dummy message poster for faster rectangle updates
        //huaweiTimer();
        //Starts all background services
        //startOneM2MForwardCommunications();
        startOneM2MBackwardCommunications();
        //startTrackingUserActivity();
        startPilotLoggingService();
        startHuaweiCommunications();

    }

    private void startPilotLoggingService() {
        Intent intent = new Intent(getApplicationContext(), PilotLogging.class);
        Log.d(TAG, "startPilotLogging: ");
        getApplicationContext().startService(intent);

    }

    private void startOneM2MBackwardCommunications() {
        Intent intent = new Intent(getApplicationContext(), OneM2MBackwardCommunications.class);
        Log.d(TAG, "startOneM2MBackwardCommunications: ");
        intent.putExtra("username", userName);
        getApplicationContext().startService(intent);

    }

    //Starts the location service that is responsible for initiating the googlefusedlocations client
    //and implements a callback whenever new location data is received to the UI thread and
    // Onem2m/Huawei.
    private void startLocationService() {
        Intent intent1 = new Intent(getApplicationContext(), GoogleFusedLocations.class);
        Log.d(TAG, "startLocationService: ");
        getApplicationContext().startService(intent1);
    }

    //Starts the service responsible for foward communications to onem2m, this receives data from
    //Locationservice and sends this info to onem2m.
    private void startOneM2MForwardCommunications() {
        Intent intent1 = new Intent(getApplicationContext(), OneM2MForwardCommunications.class);
        intent1.putExtra("username", userName);
        Log.d(TAG, "startOneM2MForwardCommunications: ");
        getApplicationContext().startService(intent1);
    }

    //Starts total Huawei communications service
    private void startHuaweiCommunications() {
        Intent intent1 = new Intent(getApplicationContext(), HuaweiCommunications.class);
        intent1.putExtra("username", userName);
        Log.d(TAG, "startOneM2MForwardCommunications: ");
        getApplicationContext().startService(intent1);
    }

    private void createBroadcastReceivers() {
        createBroadcastReceiverMotionplanningPath();
        createBroadcastReceiverLayoutChecker();
        createBroadcastReceiverResolutionGPS();
        createBroadcastReceiverCarDataRTK();
        createBroadcastReceiverCarDataHuawei();
        createBroadcastReceiverTaxiNotifcationNeeded();
        createBroadcastReceiverVRUData();
        createBroadcastReceiverTaxiCaller();
    }

    final Map<String, LatLng> map = new HashMap<>();
    List<LatLng> list;

    private void createBroadcastReceiverVRUData() {
        /*map.put("!2321", new LatLng(51.447893883296565, 5.48882099800934));
        map.put("!2321", new LatLng(51.44789382565, 5.4888204));
        map.put("!2231", new LatLng(51.447780625, 5.489011));
        initializeHeatMap(map);*/

        broadcastReceiverVRUData = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String userId = intent.getStringExtra("VRUId");
                buildVRUCircle(userId,intent.getDoubleExtra("latitude", 0),
                        intent.getDoubleExtra("longitude", 0));
                LatLng gps = new LatLng(
                        intent.getDoubleExtra("latitude", 0),
                        intent.getDoubleExtra("longitude", 0));

                Log.d("THIS IS GPS", gps.toString());
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("VRUData");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverVRUData, intentFilter);
    }

    private void buildVRUCircle(String userId, Double latitude, Double longitude) {
        Log.d(TAG, "buildVRUCircle: " + userId);
        Log.d(TAG, "buildVRUCircle: " + VRUIdVector.contains(userId));
        if (VRUIdVector == null) {
            if (userId.equals("car") || userId.equals("3") || userId.equals("2")) {
                Circle circle2 = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(latitude, longitude))
                        .radius(1)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.GREEN));
                circle2.setTag(userId);
                VRUCircleList.add(circle2);
            }
            Log.d(TAG, "buildVRUCircle: null");
            VRUIdVector.add(userId);
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(1)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.BLUE));
            circle.setTag(userId);
            VRUCircleList.add(circle);
        }
        if (VRUIdVector.contains(userId)) {
            Log.d(TAG, "buildVRUCircle: Iterer");
            for (Circle circle : VRUCircleList) {
                Log.d(TAG, "buildVRUCircle: Iterer1" + circle.getTag());
                if (circle.getTag() != null && circle.getTag().equals(userId)) {
                    if (circle.getTag().equals("car")) {
                        circle.setFillColor(Color.GREEN);
                        circle.setStrokeColor(Color.GREEN);
                    }
                    if (circle.getTag().equals("3") || circle.getTag().equals("2")) {
                        circle.setFillColor(Color.RED);
                        circle.setStrokeColor(Color.RED);
                    }
                    Log.d(TAG, "buildVRUCircle: ItererTRUE");
                    circle.setCenter(new LatLng(latitude, longitude));
                }
            }
        } else {
            Log.d(TAG, "buildVRUCircle: else");
            VRUIdVector.add(userId);
            if (userId.equals("car") || userId.equals("3") || userId.equals("2")) {
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(latitude, longitude))
                        .radius(1)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.GREEN));
                circle.setTag(userId);
                VRUCircleList.add(circle);
            }
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(1)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.BLUE));
            circle.setTag(userId);
            VRUCircleList.add(circle);
        }
    }

    private void buildTaxiCallCircle(String userId, Double latitude, Double longitude, String valid) {
        if (valid.equals("false")) {
            if (taxiCALLcircle != null) {
                taxiCALLcircle.remove();
            }
        } else {
            if (!TAXIVruCircle) {
                taxiCALLcircle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(latitude, longitude))
                        .radius(1)
                        .strokeColor(Color.RED)
                        .fillColor(Color.RED));
                taxiCALLcircle.setTag(userId + "Taxi");
                TAXIVruCircle = true;
            } else if (TAXIVruCircle) {
                taxiCALLcircle.remove();
                TAXIVruCircle = false;
            }
        }
    }

    private void createBroadcastReceiverMotionplanningPath() {
        broadcastReceiverMotionplanningPath = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //buildTaxiUnderwayNotification();
                double[] MPlat = intent.getDoubleArrayExtra("MPlat");
                double[] MPlon = intent.getDoubleArrayExtra("MPlon");
                LatLng[] points = new LatLng[MPlat.length];
                for (int i = 0; i < MPlat.length; i++) {
                    points[i] = new LatLng(MPlat[i], MPlon[i]);
                    Log.d(TAG, "oneM2MMessagesHandler: " + MPlat[i] + "," +
                            MPlon[i]);
                }
                //Function that use the points in the rectangle for visualization of position and speed
                motionPlanningPath(points);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OneM2MBackwardCommunications.SEND_MP_PATH");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverMotionplanningPath, intentFilter);

    }

    private void createBroadcastReceiverTaxiNotifcationNeeded() {
        BroadcastReceiver broadcastReceiverTaxiNotificationNeeded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                taxiNotificationNeeded = intent.getBooleanExtra("taxiNotificationNeeded",
                        false);
                Log.d(TAG, "onReceive: TaxiNotification" + taxiNotificationNeeded);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("TaxiNotificationBoolean");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverTaxiNotificationNeeded, intentFilter);

    }

    private void createBroadcastReceiverCarDataHuawei() {
        broadcastReceiverCarDataHuawei = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleCarNotificationHuawei(intent.getBooleanExtra("isInRectangle", false));
                double[] rectangleLat = intent.getDoubleArrayExtra("rectangleLat");
                double[] rectangleLon = intent.getDoubleArrayExtra("rectangleLon");
                LatLng[] points = new LatLng[5];
                for (int i = 0; i < rectangleLat.length; i++) {
                    points[i] = new LatLng(rectangleLat[i], rectangleLon[i]);
                }
                //Function that use the points in the rectangle for visualization of position and speed
                speedPolygon(points);
                geoFencingCarPolygon(points);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("HuaweiCommunications.CAR_DATA");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverCarDataHuawei, intentFilter);
    }

    private void createBroadcastReceiverResolutionGPS() {
        BroadcastReceiver ResolutionGPSReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    int REQUEST_CHECK_SETTINGS = 0x1;
                    GoogleFusedLocations.rae.startResolutionForResult(GpsActivity.this, REQUEST_CHECK_SETTINGS);
                    startTrackingUserActivity();
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GoogleFusedLocations.RESOLUTION_REQUIRED");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                ResolutionGPSReceiver, intentFilter);
    }

    private void createBroadcastReceiverLayoutChecker() {
        broadcastReceiverLayoutChecker = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isLoggingSwitched = loggingSwitch.isChecked();
                boolean isExperimentEmpty = experimentNumberText.getText().toString().isEmpty();
                boolean isRunEmpty = runNumberText.getText().toString().isEmpty();
                if (isLoggingSwitched && !isExperimentEmpty && !isRunEmpty) {
                    broadcastUIInfo(true, runNumberText.getText().toString(), experimentNumberText.getText().toString());
                } else {
                    broadcastUIInfo(false, null, null);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OneM2MForwardCommunications.LAYOUT_CHECK");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverLayoutChecker, intentFilter);
    }

    private void broadcastUIInfo(boolean isLoggingEnabled, String runNumber, String experimentNumber) {
        Intent intent = new Intent();
        intent.setAction("GpsActivity.LAYOUT_RESPONSE");
        Log.d(TAG, "responding to layout check");
        intent.putExtra("loggingEnabled", isLoggingEnabled);
        if (isLoggingEnabled) {
            intent.putExtra("runNumber", runNumber);
            intent.putExtra("experimentNumber", experimentNumber);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUploadLogs() {
        Intent intent = new Intent();
        intent.setAction("PilotLogging.upload");
        Log.d(TAG, "Broadcasting log upload");
        intent.putExtra("uploadLogs", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
                                hideFragment(campusCar);
                                break;
                            case R.id.action_car:
                                if (campusCar == null) {
                                    campusCar = new CampusCar();
                                    addFragment(campusCar);
                                }
                                showFragment(campusCar);
                                break;
                            case R.id.action_settings:
                                break;
                        }
                        return true;
                    }
                });
    }

    // Removes all fragments that are on the stack, fragments are stored on top of eachother on a
    // stack (sort of memory) and can be popped (removed), this goes back to initial google maps
    // fragment.
    private void removeFragments() {
        android.app.FragmentManager fm = getFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
        }
    }

    // Switches the fragmentcontainer which contains google maps (initially) to a certain other
    // fragment, this is passed to this function from its implementation.
    private void addFragment(Fragment fragment) {
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().add(R.id.map, fragment).addToBackStack(null).commit();
    }

    private void hideFragment(Fragment fragment) {
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().hide(fragment).addToBackStack(null).commit();
    }

    private void showFragment(Fragment fragment) {
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().show(fragment).addToBackStack(null).commit();
    }

    @Override
    protected void onPause() {
        broadcastUploadLogs();
        super.onPause();
    }

    //Override function called when google maps is first initialized/ready in the app. All campus
    // buildings are initialized (their actions/listeners) as well as the car overlay and user
    // location that is observable in the app.
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // coordinates of gemini building, ~ centre of tu/e
        CameraUpdate point = CameraUpdateFactory.newLatLngZoom(new LatLng(51.447433, 5.4908978), 15.0f);
        // moves camera to coordinates
        mMap.moveCamera(point);
        // animates camera to coordinates
        mMap.animateCamera(point);

//        addHeatMap();

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

        //mMap.setMyLocationEnabled(true);
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
        createBroadcastReceivers();
    }


    // Removes last location of Huawei geofencing rectangle and adds the new location of the
    // rectangle in the map, this function accepts array of LatLng points.
    private void motionPlanningPath(LatLng[] points) {

        /*LatLng points[] =  new LatLng[4];
        points[0] = new LatLng(51.447893883296565,5.48882099800934);
        points[1] = new LatLng(51.447893883296565,5.48882099800934);
        points[2] = new LatLng(51.4476933362316231, 5.4886143623624634);
        points[3] = new LatLng(51.447780623423362, 5.488190624624625);*/
        if (polylineMP == null) {
            polylineMP = mMap.addPolyline((new PolylineOptions()
                    .add(points)
                    .zIndex(0)
                    .color(Color.BLUE)));
            Log.d(TAG, "motionPlanningPath: ");
        } else {
            polylineMP.remove();
            polylineMP = mMap.addPolyline(new PolylineOptions()
                    .add(points)
                    .zIndex(0)
                    .color(Color.BLUE));
            Log.d(TAG, "motionPlanningPath: ");
        }
    }

    //Puts car somewhere on the map, to be later called when coordinates change.
    private void setupCarOverlay() {
        BitmapDrawable carBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.caricon);
        carBitmap = carBitmapDrawable.getBitmap();

        carOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .position(new LatLng(50.967455, 5.943757), 4)
                .image(BitmapDescriptorFactory.fromBitmap(carBitmap))
                .zIndex(1)
                .bearing(315));
    }

    //Creates notification when the taxi starts to go to your location
    private void buildTaxiUnderwayNotification() {
        if (taxiNotificationNeeded) {
            mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setContentText("")
                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setContentTitle("Autonomous car coming to your location")
                    .setOngoing(false)
                    .setAutoCancel(true);
            mNotificationManager.notify(TAXI_COMING_NOTIFICATION_ID, mBuilder.build());
            notificationArray[TAXI_COMING_NOTIFICATION_ID] = true;
            Log.d(TAG, "TaxiUnderway notification Built");
            taxiNotificationNeeded = false;
        }
    }


    // Builds common notification settings; vibration pattern, title etc which is being sent from
    // the implementation.
    private void buildCarNotification(String title) {
        long[] vibrationPattern = {Long.valueOf(0), Long.valueOf(500)};
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
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, intent.FLAG_ACTIVITY_SINGLE_TOP);
        //mBuilder.setContentIntent(intent);

    }

    // Cancels and shows notifications changed accordingly after being built in buildCarNotifcation
    // depending upon the distance in a straight line from the last car location to the last user
    // location.
    private void handleCarNotification(Double deltaMeters) {
        if (deltaMeters <= 40 && !notificationArray[AUTONOMOUS_CAR_40M_NOTIFICATION_ID]) {
            cancelNotification(AUTONOMOUS_CAR_100M_NOTIFICATION_ID);
            if (carNotificationConstant == 0) {
                mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .setSound(AUTONOMOUS_CAR_25M_NOTIFICATION_SOUND)
                        .setContentText(AUTONOMOUS_CAR_40M_NOTIFICATION)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setContentTitle("Autonomous car warning")
                        .setAutoCancel(false);
                mNotificationManager.notify(AUTONOMOUS_CAR_40M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_40M_NOTIFICATION_ID] = true;
                carNotificationConstant = 1;
            } else if (carNotificationConstant == 1) {
                mBuilder.setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentText(AUTONOMOUS_CAR_40M_NOTIFICATION)
                        .setVibrate(null)
                        .setContentTitle("Autonomous car warning")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(AUTONOMOUS_CAR_40M_NOTIFICATION));
                mNotificationManager.notify(AUTONOMOUS_CAR_40M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_40M_NOTIFICATION_ID] = true;
            }
        }
        if (deltaMeters >= 40 && deltaMeters <= 100 && !notificationArray[1]) {
            cancelNotification(AUTONOMOUS_CAR_40M_NOTIFICATION_ID);
            if (carNotificationConstant2 == 0) {
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(AUTONOMOUS_CAR_100M_NOTIFICATION));
                mBuilder.setSound(null);
                mBuilder.setContentTitle("Autonomous car warning");
                mBuilder.setContentText(AUTONOMOUS_CAR_100M_NOTIFICATION);
                mNotificationManager.notify(AUTONOMOUS_CAR_100M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_100M_NOTIFICATION_ID] = true;
            } else if (carNotificationConstant2 == 1) {
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setContentText(AUTONOMOUS_CAR_100M_NOTIFICATION);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(AUTONOMOUS_CAR_100M_NOTIFICATION));
                mBuilder.setSound(null);
                mBuilder.setContentTitle("Autonomous car warning");
                mNotificationManager.notify(AUTONOMOUS_CAR_100M_NOTIFICATION_ID, mBuilder.build());
                notificationArray[AUTONOMOUS_CAR_100M_NOTIFICATION_ID] = true;
            }
        }
        if (deltaMeters > 100) {
            cancelNotification(AUTONOMOUS_CAR_40M_NOTIFICATION_ID);
            cancelNotification(AUTONOMOUS_CAR_100M_NOTIFICATION_ID);
        }
        if (deltaMeters > 48) {
            carNotificationConstant = 0;
        }
        if (deltaMeters > 100) {
            carNotificationConstant2 = 0;
        }
    }

    // Cancels any notification. The id is the id given to the notifcation when created. the
    // notifcationarray with the element of this ID is set to false and the notifcation itself is
    // cancelled via notifcationmanager.
    public void cancelNotification(int id) {
        if (notificationArray[id]) {
            mNotificationManager.cancel(id);
            notificationArray[id] = false;
        }
    }

    // Starts the tracking of UserActivity via BackgroundDetectedAcitiviesService, which sends
    // activities list to DetectedActivitiesIntentService, this gets the highest confidense activity
    // type and broadcasts to setBroadcastReceiver
    private void startTrackingUserActivity() {
        Intent intent = new Intent(getApplicationContext(), BackgroundDetectedActivitiesService.class);
        Log.d(TAG, "startTrackingUserActivity: ");
        getApplicationContext().startService(intent);
    }

    private void createBroadcastReceiverCarDataRTK() {
        broadcastReceiverCarRTK = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                carLat = intent.getDoubleExtra("carLat", '0');
                carLon = intent.getDoubleExtra("carLon", '0');
                carSpeed = intent.getFloatExtra("carSpeed", '0');
                carHeading = intent.getFloatExtra("carHeading", '0');
                carLoc = new LatLng(carLat, carLon);
                carOverlay.setPosition(carLoc);
                Double deltaMeter = intent.getDoubleExtra("deltaMeter", 1000);
                if (carHeading < 0) {
                    carHeading = 360 + carHeading;
                }
                carOverlay.setBearing(carHeading);
                handleCarNotification(deltaMeter);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OneM2MBackwardCommunications.RTK_CAR");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverCarRTK, intentFilter);
    }

    private void createBroadcastReceiverTaxiCaller() {
        BroadcastReceiver taxiCaller = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Double latitude = intent.getDoubleExtra("latitude", '0');
                Double longitude = intent.getDoubleExtra("longitude", '0');
                String taxiCallID = intent.getStringExtra("TaxiCallID");
                String valid = intent.getStringExtra("valid");
                buildTaxiCallCircle(null, latitude, longitude, valid);
                Log.d(TAG, "onReceive: taxi call received" + latitude + ", " + longitude + "," + valid);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("TAXICircle");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                taxiCaller, intentFilter);
    }

    // Makes a tansformation of the speed into distance between points of the existing Huawei
    // rectangle, then fills this up depending on speed with a certain color. Takes the points of
    // the huawei rectangle.
    private void speedPolygon(LatLng[] points) {
        LatLng[] pointsSpeed = new LatLng[4];
        pointsSpeed[0] = points[0];
        pointsSpeed[1] = points[3];
        if (carSpeed != null) {

            //Calculates the differences in lat,lon in meters, but scaled at 35 km/h (9.7 m/s), to visualise speed
            Double DeltaLat = ((-81.5 * Math.cos(carHeading * Math.PI / 180)) / 9.7) * carSpeed;
            Double Deltalong = ((-81.5 * Math.sin(carHeading * Math.PI / 180)) / 9.7) * carSpeed;

            //point for array element 2
            Double lat2_2 = points[3].latitude - (DeltaLat * 360 / 40075000);
            Double lon2_2 = points[3].longitude - (Deltalong * 360 / 40075000);
            pointsSpeed[2] = new LatLng(lat2_2, lon2_2);


            //point for array element 3
            Double lat2_3 = points[0].latitude - (DeltaLat * 360 / 40075000);
            Double lon2_3 = points[0].longitude - (Deltalong * 360 / 40075000);
            pointsSpeed[3] = new LatLng(lat2_3, lon2_3);


            //fills up the speedpolygon with different colours based on its speed
            if (carSpeed < 2.8) {

                if (speedPolygon == null) {
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.WHITE)
                            .fillColor(Color.WHITE));
                }

                //* else {
                if (speedPolygon != null) {
                    speedPolygon.remove();
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.WHITE)
                            .fillColor(Color.WHITE));

                }

            } else if (carSpeed >= 2.8 && carSpeed <= 4.2) {

                if (speedPolygon == null) {
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.LTGRAY)
                            .fillColor(Color.LTGRAY));
                }

                //* else {
                if (speedPolygon != null) {
                    speedPolygon.remove();
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.LTGRAY)
                            .fillColor(Color.LTGRAY));

                }
            } else if (carSpeed > 4.2 && carSpeed <= 5.5) {

                if (speedPolygon == null) {
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.GRAY)
                            .fillColor(Color.GRAY));
                }

                //* else {
                if (speedPolygon != null) {
                    speedPolygon.remove();
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.GRAY)
                            .fillColor(Color.GRAY));

                }
            } else if (carSpeed > 5.5 && carSpeed <= 6.9) {

                if (speedPolygon == null) {
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.DKGRAY)
                            .fillColor(Color.DKGRAY));
                }

                //* else {
                if (speedPolygon != null) {
                    speedPolygon.remove();
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.DKGRAY)
                            .fillColor(Color.DKGRAY));

                }
            } else if (carSpeed > 6.9 && carSpeed <= 8.3) {

                if (speedPolygon == null) {
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.BLACK)
                            .fillColor(Color.BLACK));
                }

                //* else {
                if (speedPolygon != null) {
                    speedPolygon.remove();
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.BLACK)
                            .fillColor(Color.BLACK));

                }
            } else if (carSpeed > 8.3 && carSpeed <= 9.7) {

                if (speedPolygon == null) {
                    speedPolygon = mMap.addPolygon(new PolygonOptions()
                            .add(pointsSpeed)
                            .zIndex(0)
                            .strokeColor(Color.RED)
                            .fillColor(Color.RED));
                }

                //* else {
                if (speedPolygon != null) {
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
        Log.d(TAG, "handleCarNotificationHuawei: " + inZone);
        if (inZone) {
            long[] vibrationPattern = {Long.valueOf(0), Long.valueOf(500)};
            if (!notificationArray[HUAWEI_NOTIFICATION_ID]) {
                String notificationText = "You are in the rectangle";
                mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .setContentText(notificationText)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setContentTitle("Autonomous car warning")
                        .setAutoCancel(false)
                        .setVibrate(vibrationPattern);
                mNotificationManager.notify(HUAWEI_NOTIFICATION_ID, mBuilder.build());
                notificationArray[HUAWEI_NOTIFICATION_ID] = true;
            }
        }
        if (!inZone) {
            cancelNotification(HUAWEI_NOTIFICATION_ID);
            Log.d(TAG, "handleCarNotificationHuawei: Inzone false");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        broadcastUploadLogs();
        Log.e(TAG, "onStop:");
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (checkPermissions()) {
        } else if (!checkPermissions()) {
            requestPermission();
        }
    }

    // Calculates the manual speed and bearing if google does not provide any (when inside for
    // example). Uses last location and new location for the calculation of this speed and bearing.
    private String[] calculateSpeedAndBearing
    (Double latitude, Double longitude, Long timeStamp) {
        String speedGPS;
        String[] speedandBearing = new String[2];

        if (lastLat == 0.00) {
            lastLat = latitude;
            lastLon = longitude;
            lastTime = timeStamp;
        } else {
            Double deltaSeconds = DifferenceUTCtoSeconds(timeStamp, lastTime) / 1000;
            Double deltaMeters = DifferenceInMeters(lastLat, lastLon, latitude, longitude);
            speedGPS = Double.toString(deltaMeters / deltaSeconds);
            String bearingGPS = Double.toString(ManualBearing(lastLat, lastLon, latitude, longitude));
            Log.d(TAG, "manualbearing: " + bearingGPS);
            speedandBearing = new String[2];
            speedandBearing[0] = speedGPS;
            speedandBearing[1] = bearingGPS;
            GeoPoint lastGeo = new GeoPoint(lastLat, lastLon);
            GeoPoint newGeo = new GeoPoint(latitude, longitude);
            List<GeoPoint> Geopoints = new ArrayList<>();
            Geopoints.add(lastGeo);
            Geopoints.add(newGeo);
            // makePolyline(Geopoints,headingLine);
            lastLat = latitude;
            lastLon = longitude;
            lastTime = timeStamp;
        }
        //TODO add a realistic threshold to prevent huge speeds at delta t goes to zero

        return speedandBearing;
    } //

    private double DifferenceUTCtoSeconds(Long timeStamp, Long timeStamp2) {
        return timeStamp - timeStamp2;
    }

    // Difference in meters (birds flight) using 'haversine' formula, gives back distance between
    // two points in doubles. the latitude and longitude are in degrees.
    private double DifferenceInMeters(Double lastLat, Double lastLon, Double lat, Double lon) {
        Double deltaPhiLon = (lon - lastLon) * Math.PI / 180;
        Double deltaPhilat = (lat - lastLat) * Math.PI / 180;
        lastLat = lastLat * Math.PI / 180;
        lat = lat * Math.PI / 180;

        Double earth = 6371e3;

        Double a = Math.sin(deltaPhilat / 2) * Math.sin(deltaPhilat / 2) + Math.cos(lastLat) * Math.cos(lat) * Math.sin(deltaPhiLon) * Math.sin(deltaPhiLon);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double d = earth * c;
        return d;
    }

    // Calculates the bearing or direction of the user based upon two location points (the last one
    // and the latest), sends this back to be used in the logging as well as in layout. Only used
    // when Google does not give any heading/bearing, hence the name manual. lat and lon in degrees.
    private double ManualBearing(Double lastLat, Double lastLon, Double lat, Double lon) {
        Double deltaPhiLon = (lon - lastLon) * Math.PI / 180;
        lastLat = lastLat * Math.PI / 180;
        lat = lat * Math.PI / 180;

        Double a = Math.sin(deltaPhiLon) * Math.cos(lat);
        Double b = Math.cos(lastLat) * Math.sin(lat) - Math.sin(lastLat) * Math.cos(lat) * Math.cos(deltaPhiLon);
        Double c = Math.atan2(a, b) * 180 / Math.PI;
        Double d;

        if (c < 0) {
            d = c + 360;
        } else {
            d = c;
        }
        return d;
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "destroy");
        FirebaseAuth.getInstance().signOut();
        GoogleFusedLocations.shouldContinue = false;
        DetectedActivitiesIntentService.shouldContinue = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        //huaweiTimerTask.cancel();
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        super.onDestroy();
    }

    /*public void cancelRequestTaxi() {
        JSONObject callTaxi = null;
        String uuid =  UUID.randomUUID().toString();
        try {
            callTaxi = VRUgps.CreateContentInstanceCallTaxi(0.000000, 0.000000, System.currentTimeMillis(),
                    userName,false,uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            publishAndLogMessage(onem2m,callTaxi.toString(),0,oneM2MVRUReqTopic,
                    LOGGING_TAXI_SENT,callTaxi.toString(), System.currentTimeMillis(),uuid);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }*/

    // Removes last location of Huawei geofencing rectangle and adds the new location of the
    // rectangle in the map, this function accepts array of LatLng points.
    private void geoFencingCarPolygon(LatLng[] points) {
        if (geoFencingPolygon == null) {
            geoFencingPolygon = mMap.addPolygon(new PolygonOptions()
                    .add(points)
                    .zIndex(0)
                    .strokeColor(Color.LTGRAY));
        } else {
            geoFencingPolygon.remove();
            geoFencingPolygon = mMap.addPolygon(new PolygonOptions()
                    .add(points)
                    .zIndex(0)
                    .strokeColor(Color.LTGRAY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userId", userName);
        outState.putString("password", password);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    // Override function for giving functionality to whenever the back button is pressed
    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        this.finishAffinity();
    }

    //Requests the user for permission of GPS, a rationale is given if needed to state the reason
    // why GPS is needed. When permissions are requested and accepted or cancelled, the override
    //  function onRequestPermissionsResult is called.
    private void requestPermission() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "We need your gps data for essential app functions and research",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(GpsActivity.this, new String[]{Manifest
                                    .permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSIONS_REQUEST_CODE);

                }
            });
            int snackbarTextId = android.support.design.R.id.snackbar_text;
            TextView textView = (TextView) snackbar.getView().findViewById(snackbarTextId);
            textView.setTextColor(Color.WHITE);
            snackbar.getView().setBackground(permissionGrantedSnackbarShape);
            snackbar.show();
        }
        Log.d(TAG, "requestPermission: ");
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
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                //TODO als de request interuppted is, hoeft in principe niks mee te gebeuren
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "You have connected to the GPS database, data is used for research and functionality",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                int snackbarTextId = android.support.design.R.id.snackbar_text;
                TextView textView = (TextView) snackbar.getView().findViewById(snackbarTextId);
                textView.setTextColor(Color.WHITE);
                snackbar.getView().setBackground(permissionGrantedSnackbarShape);
                snackbar.show();
                startLocationService(); //TODO start service
            } else {
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
            MainActivity Login = new MainActivity();
            if (userName != null) {
                Login.login(userName + "@random.com", password);
                Log.d(TAG, "userCheck: User is null");
            } else if (userNameStored != null) {
                Login.login(userNameStored, passwordStored);
            } else {
                Intent loginIntent = new Intent(GpsActivity.this, MainActivity.class);
                startActivity(loginIntent);
            }
        }
    }

    private void initializeHeatMap(Map<String, LatLng> map) {

        // Create a heat map tile provider, passing it the latlngs of the concentrated buildings/areas
//        if (VRUIdVector.contains(userId)) {
//            // Check which m overlay is for which user id
//            //            // mOverlay.remove();
//            //            // Check which provider has which userid and then change the gps coordinates
//        } else {
        list = new ArrayList<LatLng>(map.values());
        Log.d("GPS coordinates", map.values().toString());

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(list).build();
        //            VRUIdVector.set(userID);

        mMap.addTileOverlay((new TileOverlayOptions()).tileProvider(mProvider));

    }
}
