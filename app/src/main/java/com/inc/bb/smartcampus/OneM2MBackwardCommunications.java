package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;

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

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class OneM2MBackwardCommunications extends IntentService {
    //GPS location global variables
    double lastLon;
    double lastLat;

    //car variables
    double carLon = 5.623863;
    double carLat = 51.475792;
    Float carHeading = null;
    Float carSpeed = null;
    Long lastRTK = null;
    boolean noRTK = true;

    //Message types for logging (what kind of log is needed)
    private final static int LOGGING_NOTNEEDED = 0;
    private final static int LOGGING_GPS = 1;
    private final static int LOGGING_STATUS = 2;
    private final static int LOGGING_VEHICLE = 3;
    private final static int LOGGING_HUAWEI_SENT = 4;
    private final static int LOGGING_HUAWEI_RECEIVED = 5;
    private final static int LOGGING_TAXI_SENT = 6;
    private final static int LOGGING_TAXI_RECEIVED = 7;

    //MQTT oneM2M login credentials, subscription and request topics and JSONs.
    MqttAndroidClient onem2m;
    String userName;
    String oneM2MVRUAeRi = "Csmartcampus";
    String oneM2MVRUAeRn = "aeSmartCampus1";
    String oneM2MVRUAePass = "smartcampuspassword";
    String CsmartcampusSubscriptionTopic = "/oneM2M/resp/server/aeSmartCampus1/json";
    String CsmartCampusCarsSubscriptionTopic = "/oneM2M/resp/server/aeTechnolution/json";

    //Broadcast variables
    BroadcastReceiver locationsBroadcastReceiver;

    //Logging layout check variables
    BroadcastReceiver layoutResponseBroadcastReceiver;
    boolean isLoggingSwitched = false;
    String experimentNumber;
    String runNumber;

    String TAG = "OneM2MBackwardCommunications";

    public OneM2MBackwardCommunications() {
        super("OneM2MBackwardCommunications");
    }

    @Override
    public void onCreate() {
        //Firebase initialization
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        userName = intent.getStringExtra("username");
        //Build onem2m client
        onem2m = buildOneM2MVRU(onem2m);
        Log.d(TAG, "onHandleIntent: ");

        //Create broadcast receivers for useracitivites and locations.
        createBroadcastReceiverLayoutResponse();
        createBroadcastReceiverLocations();
        //Creates settings to be passed to google's fusion location client and sets callback
        //This is the core of the app
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

    private void createBroadcastReceiverLocations() {
        locationsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                lastLat = intent.getDoubleExtra("latitude", '0');
                lastLon = intent.getDoubleExtra("longitude", '0');
                Log.d(TAG, "onReceive: ");
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GoogleFusedLocations.SEND_NEW_LOCATION");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationsBroadcastReceiver, intentFilter);
    }

    // Builds the OneM2M broker connection, subscribes to the VRU ae Response topic and creates
    // UserID container.
    private MqttAndroidClient buildOneM2MVRU(MqttAndroidClient mMqttAndroidClient) {
        // userId1 = userId1.replace("s","suser");
        String mqttBrokerUrl = "tcp://vmi137365.contaboserver.net:1883";
        mMqttAndroidClient = getMqttClient(getApplicationContext(), mqttBrokerUrl, userName + "2");
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
                    Log.d(TAG, "Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The Connection was lost." + cause.toString());
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
                    Long timeUnix = System.currentTimeMillis();
                    oneM2MMessagesHandler(topic, message, timeUnix);
                }
            });
        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
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

    // Initializes MQTT client
    public MqttAndroidClient getMqttClient(@NonNull Context context, @NonNull String brokerUrl,
                                           @NonNull String clientId) {
        final MqttAndroidClient mqttClient = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = mqttClient.connect(setMqttConnectionOptions());
            if (token == null) {
                Log.d(TAG, "token is null");
            }
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "getMqttClient: Success");
                    OneM2MMqttJson VRU = new OneM2MMqttJson(oneM2MVRUAeRi, oneM2MVRUAePass,
                            oneM2MVRUAeRn, userName);
                    subscribeToTopic(CsmartcampusSubscriptionTopic);
                    subscribeToTopic(CsmartCampusCarsSubscriptionTopic);
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

    void broadcastIsLoggingEnabled() {
        Intent intent = new Intent();
        intent.setAction("OneM2MForwardCommunications.LAYOUT_CHECK");
        Log.d(TAG, "checking if logging enabled");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // Processes the messages that are incoming from oneM2M from a certain subscribed topic as
    // defined in subscribeToTopic. This is the handler for incoming car messages and if this is
    // from the RTK (accurate GPS) from the car or from the flowradar. Currently only RTK is used.
    public void oneM2MMessagesHandler(String topic, MqttMessage message, Long timeUnix) throws JSONException {
        boolean newData=false;
        broadcastIsLoggingEnabled();
        JSONObject messageCar = new JSONObject(new String(message.getPayload()));
        Log.d(TAG, "oneM2MMessagesHandler: Arrived");

        if (topic.equals(CsmartCampusCarsSubscriptionTopic)) {
            Log.d(TAG, "oneM2MMessagesHandler: SubCar");

           /* String comparator = messageCar.getJSONObject("m2m:rsp").getString("rqi");
            String contentCarString = messageCar.getJSONObject("m2m:rqp").getJSONObject("pc")
                    .getJSONArray("m2m:sgn").getJSONObject(0).getJSONObject("nev")
                    .getJSONObject("rep").getJSONObject("m2m:cin").getString("con");*/

            String comparator = messageCar.getJSONObject("m2m:rsp").getString("rqi");
            String carUuid = null;
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
            if (comparator.equals("CREATE:prius/GPS")) {
                JSONObject contentCar = new JSONObject(messageCar.getJSONObject("m2m:rsp").getJSONObject("pc")
                        .getJSONArray("m2m:cin").getJSONObject(0).getString("con"));
                noRTK = false;
                Log.d(TAG, "oneM2MMessagesHandler: RTK");
                Log.d(TAG, "oneM2MMessagesHandler: "+contentCar);
                lastRTK = System.currentTimeMillis();
                String longitudeCarString = contentCar.getString("lon");
                carLon = Double.parseDouble(longitudeCarString);
                carSpeed = (float) contentCar.getDouble("speed");
                String latitudeCar = contentCar.getString("lat");
                carHeading = (float) contentCar.getDouble("heading");
                carUuid = contentCar.getString("UUID");
                carLat = Double.parseDouble(latitudeCar);
                newData = true;
                if (isLoggingSwitched) {
                    Intent logIntent = new Intent();
                    logIntent.setAction("OneM2M.BackwardLogging");
                    Log.d(TAG, "oneM2MMessagesHandler: LoggingRTK");
                    logIntent.putExtra("messageType", LOGGING_VEHICLE);
                    logIntent.putExtra("logmsg", contentCar.toString());
                    logIntent.putExtra("uuid", carUuid);
                    logIntent.putExtra("username", userName);
                    logIntent.putExtra("runNumber", runNumber);
                    logIntent.putExtra("experimentNumber", experimentNumber);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(logIntent);
                }
            }
                /* String[] separated = contentCarString.split(",");
                JSONObject carString =  new JSONObject();
                carString = contentCarString
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
                newData = true;} */

            /*if(comparator.equals("CREATE:prius/Motionplanning")) {
                Log.d(TAG, "oneM2MMessagesHandler: motionplanning");
                JSONObject contentMP = new JSONObject(messageCar.getJSONObject("m2m:rsp").getJSONObject("pc")
                        .getJSONArray("m2m:cin").getJSONObject(0).getString("con"));
                if (contentMP.getString("mobileId").equals(userName)) {
                    Log.d(TAG, "oneM2MMessagesHandler: motionplanning2");
                    cancelRequestTaxi();
                    motiongPlanningResponseReceived = true;
                    JSONArray jsonArray = contentMP.getJSONArray("coords");

                    LatLng[] points = new LatLng[jsonArray.length()];
                    double[] rectangleLat = new double[jsonArray.length()];
                    double[] rectangleLon = new double[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        // Array points1[][] =  new Array[jsonArray.length()][2];
                        //points[i][1] = jsonArr
                        JSONArray jArrayIter = jsonArray.getJSONArray(i);
                        Log.d(TAG, "doInBackground: " + jArrayIter);
                        rectangleLat[i] = jArrayIter.getDouble(0);
                        rectangleLon[i] = jArrayIter.getDouble(1);
                    }
                    for (int i = 0; i < rectangleLat.length; i++) {
                        points[i] = new LatLng(rectangleLat[i], rectangleLon[i]);
                        Log.d(TAG, "oneM2MMessagesHandler: "+ rectangleLat[i] + "," + rectangleLon[i]);
                    }
                    Log.d(TAG, "oneM2MMessagesHandler: motionplanning3");
                    motionPlanningPath(points);
                    String carMPUuid = contentMP.getString("responseUUID");
                    pilotLogging(LOGGING_TAXI_RECEIVED, 0, contentMP.toString(), carMPUuid);
                }
            }*/

            if (newData) {
                Intent intentCar = new Intent();
                intentCar.setAction("OneM2MBackwardCommunications.RTK_CAR");
                Log.d(TAG, "oneM2MMessagesHandler: broadcastingRTK");
                intentCar.putExtra("carSpeed", carSpeed);
                intentCar.putExtra("carHeading", carHeading);
                intentCar.putExtra("carLat", carLat);
                intentCar.putExtra("carLon", carLon);
                intentCar.putExtra("deltaMeters", differenceInMeters(carLat, carLon, lastLon, lastLat));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentCar);
            }
        }

        /*else if(topic.equals(CsmartcampusSubscriptionTopic)){
            if(messageCar.getJSONObject("m2m:rsp").getString("rqi").equals(userName)){
                String contentTimeString = messageCar.getJSONObject("m2m:rsp").getJSONObject("pc")
                        .getJSONArray("m2m:cin").getJSONObject(0)
                        .getString("rn");
                Long timeGps = Long.parseLong(contentTimeString);
                Long deltaTime = timeUnix - timeGps;
                String latencyFromGPSTillReceive = Long.toString(deltaTime);
                Log.d(TAG, "Latency:" + latencyFromGPSTillReceive);
            }
        }*/
    }

    // Difference in meters (birds flight) using 'haversine' formula, gives back distance between
    // two points in doubles. the latitude and longitude are in degrees.
    private double differenceInMeters(Double lastLat, Double lastLon, Double lat, Double lon) {
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

}