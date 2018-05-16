package com.inc.bb.smartcampus;

import android.Manifest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
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
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class GpsActivity extends AppCompatActivity implements MapViewConstants,okHttpPost.AsyncResponse{

    GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String TAG = "GpsActivity";
    private FusedLocationProviderClient mFusedLocationClient;
    private TextView viewLatitude;
    private TextView viewLongitude;
    private TextView viewLocation;
    private TextView viewBearing;
    private TextView viewmanualBearing;
    private TextView viewBearingAccuracy;
    private TextView viewSpeed;
    private TextView viewGoogleSpeed;
    private Location mCurrentlocation;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;
    private String mLastUpdateTime;
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
    private String userId;
    private String intentString;
    private LocationRequest mLocationRequest;
    private Drawable drawable1;
    int[] t= new int[2];
    Integer k=0;
    Integer timeDifferenceTotal = 0;
    IMapController mapController;
    private MapView map;

    Double lastLat=0.00;
    Double lastLon=0.00;
    Long lastTime;
    Integer i=0;
    SimpleLocationOverlay personOverlay;
    SimpleLocationOverlay carOverlay;
    SimpleLocationOverlay BuildingMarker;
    Polyline headingLine;
    private PendingIntent mActivityRecognitionPendingIntent;
    Thread oneM2MGPSThread;

    //MQTT String and variables
    MqttAndroidClient onem2m,onem2m2;
    String oneM2MVRUAeRi = "Csmartcampus";
    String oneM2MVRUAeRn = "aeSmartCampus1";
    String oneM2MVRUAePass = "smartcampuspassword";
    String oneM2MVRUReqTopic = "/oneM2M/req/Csmartcampus/server/json";
    private final static int CREATE = 1;
    private final static int RETRIEVE = 2;
    private final static int UPDATE = 3;
    private final static int DELETE = 4;
    String CsmartcampusSubscriptionTopic = "/oneM2M/resp/server/Csmartcampus/json";
    String CsmartCampusCarsSubscriptionTopic = "/oneM2M/resp/server/Ctechnolution/json";
    BroadcastReceiver broadcastReceiver;
    String userActivityType;
    int userActivityTypeInt=20;
    int userConfidence = 100;
    String mActivityRecognitionTimestamp;
    SimpleDateFormat df;
    OneM2MMqttJson VRUgps;
    String huaweiUrl = "http://217.110.131.79:2020/mobile/dataapp";
    JSONObject contentCreateGPS, contentCreateUserStatus;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = mAuth.getCurrentUser();
        userCheck(user);
        getUsername(user);
        Log.d(TAG, "onCreate: " + userId);

        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_gps);
        Drawable locButtondrawableBefore = ContextCompat.getDrawable(getApplicationContext(), R.drawable.buttonshapebefore);
        Button locButton = (Button) findViewById(R.id.locButton);
        locButton.setBackground(locButtondrawableBefore);
        Log.d(TAG, "onCreate: ");


    // GPS functionality, maybe in thread, maybe not, APP keeps doing thread after App quits.
        //TODO fix GPS function continueing after onDestroy when in thread

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




        setupMap();
        ///Git test


        createVRUJSONS();
        drawable1 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.snackbarshape);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        viewLatitude = (TextView) findViewById(R.id.latitude);
        viewLongitude = (TextView) findViewById(R.id.longitude);
        viewBearing = (TextView) findViewById(R.id.bearing);
        viewBearingAccuracy = (TextView) findViewById(R.id.bearingAccuracy);
        viewLocation = (TextView) findViewById(R.id.location);
        viewSpeed = (TextView) findViewById(R.id.speed);
        viewmanualBearing = (TextView) findViewById(R.id.manualbearing);
        viewGoogleSpeed = (TextView) findViewById(R.id.Googlespeed);


        updateValuesFromBundle(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .build();
        mGoogleApiClient.connect();

        //UserActivityRecognition



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

    private void createVRUJSONS() {
         VRUgps = new OneM2MMqttJson(oneM2MVRUAeRi,oneM2MVRUAePass,oneM2MVRUAeRn,userId);
        try {
            contentCreateGPS = VRUgps.CreateContentInstanceGps(null,null,null,null);
            contentCreateUserStatus = VRUgps.CreateContentInstanceStatus(null ,null,0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setBroadcastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ConstantsClassifier.BROADCAST_DETECTED_ACTIVITY)) {
                    userActivityTypeInt = intent.getIntExtra("type",20);
                    userConfidence= intent.getIntExtra("confidence", 0);
                    Long Timestamp = intent.getLongExtra("timestamp",0);

                    userActivityType = checkActivityType(userActivityTypeInt);
                    mActivityRecognitionTimestamp = df.format(Timestamp);
                    //TODO make container for GPS and Activity seperately
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
                broadcastReceiver, new IntentFilter("activity_intent"));
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

                if (reconnect) {
                    Log.d(TAG, ("Reconnected to : " + serverURI));
                    //subscribeToTopic(CsmartcampusSubscriptionTopic);

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
                            vehicleIconandParse(topic,message);

                        }
                    });
                } catch (MqttException ex){
                System.err.println("Exception whilst subscribing");
                ex.printStackTrace();
            }
    }//Subscribes to response topic

    private void vehicleIconandParse(String topic, MqttMessage message) throws JSONException {
        JSONObject messageCar = new JSONObject(new String(message.getPayload()));
        Log.d(TAG, "messageArrived: " + messageCar);
        if(topic.equals(CsmartCampusCarsSubscriptionTopic)){
            String contentCar = messageCar.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONArray("m2m:sgn").getJSONObject(0).getJSONObject("nev").getJSONObject("rep").getJSONObject("m2m:cin").getString("con");

            String contentCarString = contentCar;
            String[] separated = contentCarString.split(",");

            String longitudeCarString = separated[3];
            String[] carLonseparated = longitudeCarString.split(":");
            Double carLon = Double.parseDouble(carLonseparated[1]);

            String speedCarString = separated[5];
            String[] speedCarSep = speedCarString.split(":");
            Double speedCar = Double.parseDouble(speedCarSep[1]);



            String headingCar = separated[6];

            String latitudeCar = (separated[4]);
            String[] carLatseparated = latitudeCar.split(":");
            Double carLat = Double.parseDouble(carLatseparated[1]);
            GeoPoint carLoc = new GeoPoint(carLat,carLon);
            locationIconUpdate(carLoc);

            if(mCurrentlocation!=null){
                Double deltaMeters;
                deltaMeters = DifferenceInMeters(carLat,carLon,mCurrentlocation.getLatitude(),mCurrentlocation.getLongitude());
            }

        }
    }

    public void publishMessage(@NonNull MqttAndroidClient client, @NonNull String msg, int qos, @NonNull String topic) throws MqttException, UnsupportedEncodingException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(5866);
        message.setRetained(true);
        message.setQos(qos);
        Log.d(TAG, "Sent message: " + new String(message.getPayload()));
        client.publish(topic, message);
    } // Publishes message to VRU ae

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

                    subscribeToTopic(CsmartCampusCarsSubscriptionTopic);
                    try {
                        JSONObject createContainerJSON = VRU.CreateContainer(userId);
                        String createContainer = createContainerJSON.toString();
                        publishMessage(onem2m,createContainer,1,oneM2MVRUReqTopic);
                        JSONObject createContainerGpsJSON = VRU.CreateUserContainer("Gps");
                        String createContainerGps = createContainerGpsJSON.toString();
                        publishMessage(onem2m,createContainerGps,1,oneM2MVRUReqTopic);
                        JSONObject createContainerStatusJSON = VRU.CreateUserContainer("Status");
                        String createContainerStatus = createContainerStatusJSON.toString();
                        publishMessage(onem2m,createContainerStatus,1,oneM2MVRUReqTopic);
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
    private void updateLocationUI() {
        if(mCurrentlocation!=null){
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSS");
            Long timestampUTC = mCurrentlocation.getTime();

            Float Accuracy = mCurrentlocation.getAccuracy();

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
            viewBearingAccuracy.setText(bearingAccuracy);



            Integer timeDifference = 0;
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

            Double Longitude = mCurrentlocation.getLongitude();
            Double Latitude = mCurrentlocation.getLatitude();

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
            viewSpeed.setText(speedGPS);
            viewmanualBearing.setText(bearing);


            //Data to Firebase, not needed atm
            /*mDatabase.child("users").child(userId).child("longitude").setValue(longitude);
            mDatabase.child("users").child(userId).child("latitude").setValue(latitude);
            mDatabase.child("users").child(userId).child("speed").setValue(speed);*/

            //Publishing gps to onem2m
            try {
                publishGpsData(Latitude,Longitude,Accuracy, timestampUTC,speedGPS,bearing);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //UI buttons and on campus test
            GeoPoint loc = new GeoPoint(Latitude,Longitude);
            String location = "lat: " + latitude + " lng: " + longitude;
            Double bound1la = 51.445110;
            Double bound2la= 51.452770;
            Double bound1lo = 5.500690;
            Double bound2lo = 5.481070;
            personIconUpdate(loc);
            onCampusTest(bound1la,bound2la,bound2lo,bound1lo, Longitude, Latitude);
            myLocationButton(loc);
            buildingIcon(loc);

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
    private void publishUserStatus(String activity, String timeStamp, int confidence) throws JSONException, MqttException, UnsupportedEncodingException {
        String con = "activity: " + activity +  "," +  " activity confidence: " + confidence;
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("con", con);
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("rn", timeStamp);
        String contentCreateStatus = contentCreateUserStatus.toString();
        publishMessage(onem2m,contentCreateStatus,0,oneM2MVRUReqTopic);}

    private void publishGpsData(Double latitude, Double longitude, Float Accuracy, Long formattedDate, String speedGPS, String manualBearing) throws JSONException, MqttException, UnsupportedEncodingException {
        String formattedDateString = "UTC"+ Long.toString(formattedDate) ;
        if(manualBearing==null){
            manualBearing="0";
        }
        String con = "{\"type\":5,\"id\":" + userId + ", \"timestampUtc\":" + formattedDate + ", \"lon\":" + longitude + ", \"lat\":"+ latitude + ", \"speed\":"+ speedGPS + ", \"heading\":"+manualBearing+"}";
        String conHuawei = "{\"type\":5,\"id\":" + userId + ", \"timestampUtc\":" + formattedDateString + ", \"lon\":" + longitude + ", \"lat\":"+ latitude + ", \"speed\":"+ speedGPS + ", \"heading\":"+manualBearing+"}";
        okHTTPPost(huaweiUrl,conHuawei);
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
        }

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
        Log.d(TAG, "DiffMetersdeltaphilon: " + deltaPhiLon);
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

        Double a = Math.sin(deltaPhiLon)*Math.cos(lat);;
        Double b = Math.cos(lastLat) * Math.sin(lat) - Math.sin(lastLat) * Math.cos(lat) * Math.cos(deltaPhiLon);
        Double c = Math.atan2(a, b)*180/Math.PI;
        Double d;
        if(c<0){
            d = c + 360;

        }
        else {
            d = c;
        }
        return d;
    };


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


    private void locationIconUpdate(GeoPoint loc) {
        if (carOverlay != null) {
            map.getOverlays().remove(carOverlay);
        }
        Bitmap locationIcon = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.locationicon)).getBitmap(), 40, 40, false);
        carOverlay = new SimpleLocationOverlay(locationIcon);
        carOverlay.setLocation(loc);
        map.getOverlays().add(carOverlay);
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
        Log.d(TAG, "destroy");
        FirebaseAuth.getInstance().signOut();
        stopTracking();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INVTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    private void myLocationButton(GeoPoint loc) {
        final GeoPoint locf = loc;
        Drawable locButtondrawableAfter = ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshape);
        Button locButton = (Button) findViewById(R.id.locButton);
        if(locButton.getBackground()!=locButtondrawableAfter){
        locButton.setBackground(locButtondrawableAfter);}
        if(locButton.getBackground()==locButtondrawableAfter){
            locButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map.getController().setCenter(locf);

                }
            });
        }
    }

    private void buildLocationSettingsRequest(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(this.mLocationRequest);
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

    } // Checks whether user gives permission to use GPS

    private void userCheck(FirebaseUser user) {
        if (user == null) {
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
                mCurrentlocation= locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();

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
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
        }
    } //Updates values from savedinstance

    private void stopTracking() {
        Intent intent = new Intent(GpsActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    } // Stops tracking of UserActivity (Called in OnDestroy)

    //TODO Button for my location perhaps
    //TODO Better image for current location
    //TODO Imageoverlay clickable for Tu campus

    //TODO !Optimize the functions so that the JSONobjects are not newly made every function call, OneM2Mmessaging is becoming slow.
}
