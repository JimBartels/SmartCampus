package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class OneM2MForwardCommunications extends IntentService {
    //GPS location global variables
    double Longitude;
    double Latitude;
    float Heading= Float.valueOf(0);
    float Accuracy= Float.valueOf(0);
    float Speed = Float.valueOf(0);

    //DetectUserActivity variables
    BroadcastReceiver broadcastReceiver;
    String userActivityType;
    int userActivityTypeInt=20;
    int userConfidence = 100;
    long mActivityRecognitionTimestamp;
    BroadcastReceiver broadcastReceiverUserActivity;
    OneM2MMqttJson VRUgps;

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
    String oneM2MVRUReqTopic = "/oneM2M/req/aeSmartCampus1/server/json";
    String CsmartcampusSubscriptionTopic = "/oneM2M/resp/server/aeSmartCampus1/json";
    String CsmartCampusCarsSubscriptionTopic = "/oneM2M/resp/server/aeTechnolution/json";
    JSONObject contentCreateGPS, contentCreateUserStatus, contentCreateCallCar;
    //OneM2M op (operand) for in json op: fields.
    private final static int CREATE = 1;
    private final static int RETRIEVE = 2;
    private final static int UPDATE = 3;
    private final static int DELETE = 4;

    //Broadcast variables
    BroadcastReceiver locationsBroadcastReceiver,cancelTaxiRequestBroadcastReceiver,
            callTaxiBroadcastReceiver;

    //Logging layout check variables
    BroadcastReceiver layoutResponseBroadcastReceiver;
    boolean isLoggingSwitched = false;
    String experimentNumber;
    String runNumber;

    String TAG = "OneM2MForwardCommunications";

    public OneM2MForwardCommunications() {
        super("OneM2MForwardCommunications");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createVRUJSONS();
        userName = intent.getStringExtra("username");
        //Build onem2m client
        onem2m = buildOneM2MVRU(onem2m);
        Log.d(TAG, "onHandleIntent: ");

        //Create broadcast receivers for useracitivites and locations.
        createBroadcastReceiverUserActivity();
        createBroadcastReceiverLocations();
        createBroadcastReceiverLayoutResponse();
        createBroadcastReceiverTaxiReceived();
        createBroadcastReceiverCallTaxi();
    }

    private void createBroadcastReceiverTaxiReceived() {
        cancelTaxiRequestBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                cancelRequestTaxi();
                Log.d(TAG, "onReceive: CancelTaxi");
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CancelTaxiRequest");
        intentFilter.addAction("CancelTaxiRequest.Received");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                cancelTaxiRequestBroadcastReceiver, intentFilter);
    }

    private void createBroadcastReceiverCallTaxi() {
        callTaxiBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: CallTaxi Receiver");
                callTaxi();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CampusCar.REQUEST_TAXI");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                callTaxiBroadcastReceiver, intentFilter);
    }

    private void createBroadcastReceiverLayoutResponse() {
        layoutResponseBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: layout");
                isLoggingSwitched = intent.getBooleanExtra("loggingEnabled",
                        false);
                Log.d(TAG, "onReceive: " + isLoggingSwitched);
                if(isLoggingSwitched){
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


    public void cancelRequestTaxi() {
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
        Intent intent = new Intent();
        intent.setAction("TaxiNotificationBoolean");
        intent.putExtra("taxiNotificationNeeded",true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // Sends a message to OneM2M CallCar container when button is clicked
    // for calling a taxi. This message is then forwarded to Csmartcampus topic for IBM rebalancing
    // service via the subscription container CallTaxi_sub and also Motionplanning of NEC
    public void callTaxi() {
        try {
            String uuid = UUID.randomUUID().toString();
            JSONObject callTaxi = VRUgps.CreateContentInstanceCallTaxi(Latitude, Longitude, System.currentTimeMillis(),
                    userName,true,uuid);
            String data = callTaxi.getJSONObject("m2m:rqp").getJSONObject("pc").
                    getJSONObject("m2m:cin").getString("con");
            Log.d(TAG, "callTaxi: " + callTaxi);
            publishAndLogMessage(onem2m,callTaxi.toString(),0,oneM2MVRUReqTopic,
                    LOGGING_TAXI_SENT,data, System.currentTimeMillis(),uuid);

        } catch (JSONException e) {
            Log.e(TAG, "CallCar: "+e.toString());
        } catch (UnsupportedEncodingException e){
            Log.e(TAG, "CallCar: "+e.toString());
        } catch (MqttException e) {
            Log.e(TAG, "CallCar: "+e.toString());
        }
    }

    // Builds the OneM2M broker connection, subscribes to the VRU ae Response topic and creates
    // UserID container.
    private MqttAndroidClient buildOneM2MVRU(MqttAndroidClient mMqttAndroidClient) {
        // userId1 = userId1.replace("s","suser");
        String mqttBrokerUrl = "tcp://vmi137365.contaboserver.net:1883";
        mMqttAndroidClient = getMqttClient(getApplicationContext(), mqttBrokerUrl, userName);
        mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, ("Reconnected to : " + serverURI));
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
                    Long timeUnix = System.currentTimeMillis();
                    //oneM2MMessagesHandler(topic,message,timeUnix);
                }
            });
        } catch (MqttException ex){
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
                                null, null);
                        publishAndLogMessage(onem2m,VRU.
                                        CreateUserContainer("Gps").toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null, null);
                        publishAndLogMessage(onem2m,VRU.
                                        CreateUserContainer("CallTaxiMotionPlanning").
                                        toString(),0, oneM2MVRUReqTopic,LOGGING_NOTNEEDED,
                                null, null, null);
                        publishAndLogMessage(onem2m,VRU.
                                        CreateContentInstanceCallTaxi(0.00000, 0.00000,
                                                System.currentTimeMillis(), userName,false,
                                                UUID.randomUUID().toString()).toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null, null);
                        publishAndLogMessage(onem2m,VRU.
                                        CreateUserContainer("Status").toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null, null);
                        publishAndLogMessage(onem2m,VRU.
                                        CreateUserContainer("CallTaxi").toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null, null);
                        publishAndLogMessage(onem2m,VRU.
                                        CreateTaxiSubContainer().toString(),0,
                                oneM2MVRUReqTopic,LOGGING_NOTNEEDED,null,
                                null, null);
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

    // Creates broadcast receiver for DetectedActivitiesIntentService, this receives all activites
    // with their confidence. The Highest confidence activity is then sent to checkActivsityType and
    // published to oneM2M and Huawei and logged.
    private void createBroadcastReceiverUserActivity(){
        broadcastReceiverUserActivity = new BroadcastReceiver() {
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
                broadcastReceiverUserActivity, new IntentFilter(ConstantsClassifier.ACTIVITY_BROADCAST_ACTION));
    }

    // Function for creating the final Json to be sent to the publishandlogmessage function for
    // sending and logging of the userstatus to oneM2M. The input concerns the confidence activity
    // type and timestamp of the received detected user activity from the AcitivtyRecognition
    // broadcast receiver.
    private void publishUserStatus(String activity, Long timeStamp, int confidence)
            throws JSONException, MqttException, UnsupportedEncodingException {
        UUID uuid = UUID.randomUUID();
        JSONObject contentinstancecontentdata = new JSONObject();
        contentinstancecontentdata.put("activity", activity);
        contentinstancecontentdata.put("activityConfidence", confidence);
        contentinstancecontentdata.put("timestampUtc", timeStamp);
        contentinstancecontentdata.put("UUID", uuid.toString());
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin")
                .put("con", contentinstancecontentdata.toString());
        String to = "/server/server/aeSmartCampus1/Users/" + userName + "/Status";
        contentCreateUserStatus.getJSONObject("m2m:rqp").put("to",to);
        String contentCreateStatus = contentCreateUserStatus.toString();
        String logmessage = contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc")
                .getJSONObject("m2m:cin").getString("con");
        publishAndLogMessage(onem2m,contentCreateStatus,0,oneM2MVRUReqTopic
                ,LOGGING_STATUS,logmessage, timeStamp, uuid.toString());
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

    private void createBroadcastReceiverLocations() {
        locationsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                broadcastIsLoggingEnabled();
                boolean shouldContinue = intent.getBooleanExtra("shouldContinue",
                        true);
                if(!shouldContinue){stopSelf();}
                Longitude = intent.getDoubleExtra("longitude",'0');
                Latitude = intent.getDoubleExtra("latitude",'0');
                Accuracy = intent.getFloatExtra("accuracy",'0');
                Heading = intent.getFloatExtra("heading",'0');
                Speed = intent.getFloatExtra("speed",'0');
                Long timeStamp = intent.getLongExtra("timeStamp",0);
                String uuid = intent.getStringExtra("uuid");
                Log.d(TAG, "onReceive: location received");
                try {
                    publishGpsData(Latitude,Longitude, Accuracy, timeStamp,String.valueOf(Speed),
                            String.valueOf(Heading),uuid);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

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
                                Long formattedDate, String speedGPS, String manualBearing,String uuid)
            throws JSONException, MqttException, UnsupportedEncodingException {
        String formattedDateString = "UTC" + Long.toString(formattedDate);
        String topic = "/server/server/" + "aeSmartCampus1" + "/Users/" + userName + "/gps";
        /*String con = "{\"type\":5,\"id\":" + userName + ",\"timestampUtc\":" + formattedDate +
                ",\"lon\":" + longitude + ",\"lat\":" + latitude + ",\"speed\":" + speedGPS +
                ",\"heading\":" + manualBearing + ",\"accuracy\":" + Accuracy + ",\"UUID\": " + "\"" + uuid + "\"" + "}";*/
        JSONObject jsonObject = new JSONObject();
        String con =  jsonObject.put("type",5).put("id",userName)
                .put("timestampUtc",formattedDate).put("lon",longitude)
                .put("lat",latitude).put("speed",speedGPS)
                .put("heading",manualBearing).put("accuracy",Accuracy)
                .put("UUID",uuid).toString();

        contentCreateGPS.getJSONObject("m2m:rqp").put("to", topic);
        contentCreateGPS.getJSONObject("m2m:rqp").put("op", CREATE);
        contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin")
                .put("con", con);
        String contentCreate = contentCreateGPS.toString();
        String logmessage = contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc")
                .getJSONObject("m2m:cin").getString("con");

        publishAndLogMessage(onem2m, contentCreate, 0, oneM2MVRUReqTopic, LOGGING_GPS,
                logmessage, formattedDate, uuid);
    }

    // Publishes any message to to a topic on oneM2M defined by the input and logs this message.
    // This is used for publishing status, GPS and call taxi messages. Messagetype defines what kind
    // of messsage this is and if it needs to be logged or not.
    public void publishAndLogMessage(@NonNull MqttAndroidClient client, @NonNull final String msg,
                                     int qos, @NonNull final String topic, final int messageType,
                                     final String logmsg, @NonNull final Long generationTimeStamp, final String uuid)
            throws MqttException, UnsupportedEncodingException {
        broadcastIsLoggingEnabled();
        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        final MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(5866);
        message.setRetained(true);
        message.setQos(qos);
        Log.d(TAG, "Sent message: " + new String(message.getPayload()));
        client.publish(topic, message).setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "onSuccess message: ");
                if(messageType!=LOGGING_NOTNEEDED) {
                    Log.d(TAG, "onSuccess: " + isLoggingSwitched);
                    if(isLoggingSwitched){
                        Intent logIntent = new Intent();
                        logIntent.setAction("OneM2M.ForwardLogging");
                        logIntent.putExtra("messageType",messageType);
                        logIntent.putExtra("logmsg",logmsg);
                        logIntent.putExtra("uuid",uuid);
                        logIntent.putExtra("generationTimeStamp",generationTimeStamp);
                        logIntent.putExtra("username",userName);
                        logIntent.putExtra("runNumber",runNumber);
                        logIntent.putExtra("experimentNumber",experimentNumber);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(logIntent);
                    }
                }
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            }
        });
    }

    private void broadcastIsLoggingEnabled(){
        Intent intent = new Intent();
        intent.setAction("OneM2MForwardCommunications.LAYOUT_CHECK");
        Log.d(TAG, "checking if logging enabled");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
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
                    0,userName,false,null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}