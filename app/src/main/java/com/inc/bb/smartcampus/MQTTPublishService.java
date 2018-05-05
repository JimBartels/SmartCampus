package com.inc.bb.smartcampus;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

public class MQTTPublishService extends Service {
    String userId,OneM2MAeRi,OneM2MAePass,OneM2MAeRn;
    OneM2MMqttJson VRUgps;
    JSONObject contentCreateGPS;
    JSONObject contentCreateUserStatus;
    String oneM2MVRUReqTopic = "/oneM2M/req/Csmartcampus/server/json";
    String userActivityType;
    int userConfidence;
    String TAG = "MQTTPublishservice";
    MqttAndroidClient onem2m;

    @Override
    public void onCreate() {
        super.onCreate();
        createVRUJSONS();
        buildOneM2MVRU();
        setBroadcastReceiver();
    }

    private void setBroadcastReceiver(){
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ConstantsClassifier.BROADCAST_DETECTED_ACTIVITY)) {
                    userActivityType = intent.getStringExtra("type");
                    userConfidence= intent.getIntExtra("confidence", 0);
                    Long Timestamp = intent.getLongExtra("timestamp",0);
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSS");
                    String mActivityRecognitionTimestamp = df.format(Timestamp);
                    //TODO make container for GPS and Activity seperately
                    Log.d(TAG, userActivityType + "From broadcast " + userConfidence);
                    try {
                        //publishGpsData();
                        publishUserStatus(userActivityType,mActivityRecognitionTimestamp,userConfidence);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter("activity_intent"));
    } // Receives broadcast from DetectedActivitiesIntentService, which sends only highest confidens UserActivity

    MQTTPublishService(String userId, String OneM2MAeRi, String OneM2MAePass, String OneM2MAeRn){
        this.userId = userId;
        this.OneM2MAePass = OneM2MAePass;
        this.OneM2MAeRi = OneM2MAeRi;
        this.OneM2MAeRn = OneM2MAeRn;

    }

    void createVRUJSONS() {
        VRUgps = new OneM2MMqttJson(OneM2MAeRi,OneM2MAePass,OneM2MAeRn,userId);
        try {
            contentCreateGPS = VRUgps.CreateContentInstanceGps(null,null,null,null);
            contentCreateUserStatus = VRUgps.CreateContentInstanceStatus(null ,null,0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void buildOneM2MVRU() {
        String mqttBrokerUrl = "tcp://vmi137365.contaboserver.net:1883";
        userId=userId.replace("@random.com", "");
        onem2m = getMqttClient(getApplicationContext(), mqttBrokerUrl, userId);
        onem2m.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.d(TAG, ("Reconnected to : " + serverURI));
                    subscribeToTopic();
                } else {
                    Log.d(TAG,"Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG,"The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG,"Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "delivery completed");
            }
        });

    } // Builds the OneM2M broker connection, subscribes to the VRU ae Response topic and creates UserID container.

    public void subscribeToTopic() {
        try {
            onem2m.subscribe("/oneM2M/resp/server/Csmartcampus/json", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG,"Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG,"Failed to subscribe");
                }
            });
        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }//Subscribes to response topic

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

    private void publishUserStatus(String activity, String timeStamp, int confidence) throws JSONException, MqttException, UnsupportedEncodingException {
        userId = userId.replace("@random.com","");
        String con = "activity: " + activity +  "," +  " activity confidence: " + confidence;
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("con", con);
        contentCreateUserStatus.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("rn", timeStamp);
        String contentCreateStatus = contentCreateUserStatus.toString();
        publishMessage(onem2m,contentCreateStatus,1,oneM2MVRUReqTopic);}
    private void publishGpsData(Double latitude, Double longitude, Float Accuracy, String formattedDate) throws JSONException, MqttException, UnsupportedEncodingException {
        userId = userId.replace("@random.com","");
        String con = "lat: " + latitude + "," + " long: " + longitude + "," + " accuracy: " + Accuracy;
        contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("con", con);
        contentCreateGPS.getJSONObject("m2m:rqp").getJSONObject("pc").getJSONObject("m2m:cin").put("rn", formattedDate);
        String contentCreate = contentCreateGPS.toString();
        publishMessage(onem2m,contentCreate,0,oneM2MVRUReqTopic);
    }

    public MqttAndroidClient getMqttClient(@NonNull Context context, @NonNull String brokerUrl, @NonNull String clientId) {
        clientId = clientId.replace("@random.com", "");
        final MqttAndroidClient mqttClient = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = mqttClient.connect(getMqttConnectionOption());
            if(token==null){Log.d(TAG, "token is null");}
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //TODO set custom disconnect options onem2m.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "getMqttClient: Success");
                    userId = userId.replace("@random.com","");
                    OneM2MMqttJson VRU = new OneM2MMqttJson(OneM2MAeRi, OneM2MAePass, OneM2MAeRn,userId);
                    subscribeToTopic();
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
        return mqttConnectOptions;
    } //Options for MQTT client (Clean session, automatic reconnect etc)
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
