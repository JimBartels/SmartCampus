package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
    private final static int LOGGING_GPS_POSEST = 11;
    private final static int LOGGING_HUAWEI_SENT_POSEST = 44;
    private final static int LOGGING_VEHICLE_POSEST = 33;
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
        /*if (topic.equals(CsmartcampusSubscriptionTopic)) {
            JSONObject contentUsers = new JSONObject(messageCar.getJSONObject("m2m:rsp").getJSONObject("pc")
                    .getJSONArray("m2m:cin").getJSONObject(0).getString("con"));
            String userId = contentUsers.getString("id");
            Double longitude = contentUsers.getDouble("lon");
            Double latitude = contentUsers.getDouble("lat");
            Log.d(TAG, "oneM2MMessagesHandler: " +userId);
            broadcastUserData(userId,longitude,latitude);
            //TODO Logging?
        }*/


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
                if(contentCar.getJSONObject("message")!=null){
                    Log.d(TAG, "oneM2MMessagesHandler: POSEST"+ contentCar.toString());
                    Intent logIntent = new Intent();
                    logIntent.setAction("OneM2M.BackwardLogging");
                    Log.d(TAG, "oneM2MMessagesHandler: LoggingRTK");
                    logIntent.putExtra("messageType", LOGGING_VEHICLE_POSEST);
                    logIntent.putExtra("logmsg", contentCar.toString());
                    String uuid = contentCar.getJSONObject("message")
                            .getJSONObject("envelope")
                            .getJSONObject("vehicleMetaData")
                            .getJSONObject("vehicleSpecificMetaData").getString("UUID");
                    logIntent.putExtra("uuid", uuid);
                    logIntent.putExtra("username", userName);
                    logIntent.putExtra("runNumber", runNumber);
                    logIntent.putExtra("experimentNumber", experimentNumber);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(logIntent);
                }
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

            if(comparator.equals("CREATE:prius/Motionplanning")) {
                Log.d(TAG, "oneM2MMessagesHandler: motionplanning");
                JSONObject contentMP = new JSONObject(messageCar.getJSONObject("m2m:rsp").getJSONObject("pc")
                        .getJSONArray("m2m:cin").getJSONObject(0).getString("con"));

                if (contentMP.getString("mobileId").equals(userName)) {
                    Log.d(TAG, "oneM2MMessagesHandler: motionplanning2");
                    sendBroadcastCancelRequestTaxi();
                    JSONArray jsonArray = contentMP.getJSONArray("coords");
                    double[] pathLat = new double[jsonArray.length()];
                    double[] pathLon = new double[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        // Array points1[][] =  new Array[jsonArray.length()][2];
                        //points[i][1] = jsonArr
                        JSONArray jArrayIter = jsonArray.getJSONArray(i);
                        Log.d(TAG, "doInBackground: " + jArrayIter);
                        pathLat[i] = jArrayIter.getDouble(1);
                        pathLon[i] = jArrayIter.getDouble(0);
                    }

                    sendBroadcastUIMotionplanningPath(new double[][] {pathLat,pathLon});

                    Log.d(TAG, "oneM2MMessagesHandler: motionplanning3");
                    String carMPUuid = contentMP.getString("responseUUID");
                    if (isLoggingSwitched) {
                        Intent logIntent = new Intent();
                        logIntent.setAction("OneM2M.BackwardLogging");
                        Log.d(TAG, "oneM2MMessagesHandler: LoggingRTK");
                        logIntent.putExtra("messageType", LOGGING_TAXI_RECEIVED);
                        logIntent.putExtra("logmsg", contentMP.toString());
                        logIntent.putExtra("uuid", carMPUuid);
                        logIntent.putExtra("username", userName);
                        logIntent.putExtra("runNumber", runNumber);
                        logIntent.putExtra("experimentNumber", experimentNumber);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(logIntent);
                    }
                }
            }

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
                intentCar.putExtra("deltaMeters", differenceInMeters(lastLat, lastLon, carLat, carLon));
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

    private void broadcastUserData(String userId, Double longitude, Double latitude) {
        Intent intent = new Intent();
        intent.putExtra("VRUId", userId);
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        intent.setAction("VRUData");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBroadcastUIMotionplanningPath(double[][] latlonMP) {
        double pathLat[] = latlonMP[0];
        double pathLon[] = latlonMP[1];
        Intent intent = new Intent();
        intent.putExtra("MPlat",pathLat);
        intent.putExtra("MPlon",pathLon);
        intent.setAction("OneM2MBackwardCommunications.SEND_MP_PATH");
        Log.d(TAG, "sendBroadcastUIMotionplanningPath: Sending path");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBroadcastCancelRequestTaxi() {
            Intent intent = new Intent();
            intent.setAction("CancelTaxiRequest.Received");
            Log.d(TAG, "sending a cancel request");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private double[][] postProcessPoints(JSONArray jsonArray) throws JSONException {
        try {
            jsonArray =  new JSONArray("[[5.621214253494322, 51.47541059025025], [5.62127524763604, 51.475414772315744], [5.621370026817552, 51.47542036701487], [5.621431033732289, 51.475423463795856], [5.621525336217586, 51.47542763410312], [5.621586811299805, 51.47543052413571], [5.621686078097918, 51.47543577362394], [5.6217127532141555, 51.475437616451075], [5.621754960615094, 51.475441168483165], [5.621777771327359, 51.475442825617584], [5.621804479938089, 51.47544432684967], [5.62192585944452, 51.475449421577636], [5.6219879087030415, 51.47545171301037], [5.622081089551268, 51.47545450933263], [5.622145680331274, 51.47545601531449], [5.622239820552249, 51.47545782358931], [5.622269561278942, 51.47545853851768], [5.622300592164824, 51.47545949717022], [5.6223330419872255, 51.475460791228514], [5.622365270457825, 51.475462323643285], [5.622523187565648, 51.47547133377415], [5.622553721542004, 51.47547279116144], [5.622584413966011, 51.475474027814386], [5.622614759538166, 51.47547496030522], [5.622675902757946, 51.4754759728059], [5.622828960260933, 51.47547763351552], [5.622858988131518, 51.47547783876785], [5.622950487000901, 51.475478018293174], [5.622982119969601, 51.47547849843513], [5.623013969071093, 51.475479708016636], [5.623045883723711, 51.475482221004874], [5.623076598106441, 51.47548647465342], [5.6231059416003415, 51.47549374768511], [5.62313154166378, 51.475505142661895], [5.623149987385574, 51.475520725568366], [5.623159264476144, 51.47553943601829], [5.623157218570495, 51.475559060768404], [5.623141664033958, 51.475577917268296]]");
        } catch (JSONException e) {
            Log.d(TAG, "motionPlanningPath: " + e.toString());
            e.printStackTrace();
        }

        LatLng[] points = new LatLng[jsonArray.length()];
        double[] rectangleLat = new double[jsonArray.length()];
        double[] rectangleLon = new double[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            // Array points1[][] =  new Array[jsonArray.length()][2];
            //points[i][1] = jsonArr
            JSONArray jArrayIter = jsonArray.getJSONArray(i);
            Log.d(TAG, "motionPlanningPath: " + jArrayIter);
            rectangleLat[i] = jArrayIter.getDouble(0);
            rectangleLon[i] = jArrayIter.getDouble(1);
        }
        /*for (int i = 0; i < rectangleLat.length; i++) {
            points[i] = new LatLng(rectangleLon[i], rectangleLat[i]);
            Log.d(TAG, "motionPlanningPath: "+ rectangleLat[i] + "," + rectangleLon[i]);
        }*/
        return new double[][]{rectangleLat, rectangleLon};
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
        Log.d(TAG, "lastlat: " + lastLat + ", " + lat + " lastlon: " + lastLon + ", " + lon);
        return d;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

}