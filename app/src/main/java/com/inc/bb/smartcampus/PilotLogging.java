package com.inc.bb.smartcampus;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class PilotLogging extends IntentService {

    String TAG = "PilotLogging";

    //Broadcaster
    BroadcastReceiver broadcastReceiverLoggingM2M,broadcastReceiverLoggingHuawei,broadcastReceiverUploadLogFiles;
    
    //Logging files
    File file;
    int experimentNumber = 0;
    int runNumber = 0;
    Vector<String> fileNameVector =  new Vector<>();

    //Logging layout widgets
    String runNumberText, experimentNumberText;
    String userName;

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

    public PilotLogging() {
        super("PilotLogging");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        createBroadcastReceiverLoggingM2MFoward();
        createBroadcastReceiverLoggingM2MBackward();
        createBroadcastReceiverUploadLogFiles();
    }

    private void createBroadcastReceiverLoggingM2MFoward() {
    broadcastReceiverLoggingM2M = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                userName = intent.getStringExtra("username");
                int messageType = intent.getIntExtra("messageType",0);
                String logmsg = intent.getStringExtra("logmsg");
                String uuid = intent.getStringExtra("uuid");
                Long generationTimeStamp = intent.getLongExtra("generationTimeStamp", (long) 0.0000);
                runNumberText = intent.getStringExtra("runNumber");
                experimentNumberText = intent.getStringExtra("experimentNumber");
                pilotLogging(messageType,generationTimeStamp,logmsg,uuid);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OneM2M.ForwardLogging");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverLoggingM2M, intentFilter);
    }

    private void createBroadcastReceiverLoggingM2MBackward() {
        broadcastReceiverLoggingM2M = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int messageType = intent.getIntExtra("messageType",0);
                String logmsg = intent.getStringExtra("logmsg");
                String uuid = intent.getStringExtra("uuid");
                experimentNumberText = intent.getStringExtra("experimentNumber");
                runNumberText = intent.getStringExtra("runNumber");
                userName = intent.getStringExtra("userName");
                pilotLogging(messageType,0,logmsg,uuid);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OneM2M.BackwardLogging");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverLoggingM2M, intentFilter);
    }

    private void createBroadcastReceiverUploadLogFiles() {
        broadcastReceiverUploadLogFiles = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean uploadLogs = intent.getBooleanExtra("uploadLogs",false);
                if(uploadLogs){uploadLogFilesFirebase();}
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PilotLogging.upload");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiverUploadLogFiles, intentFilter);
    }

    // Function that processes al logging depending on the messagetype (what kind of log), the data
    // and the generation timestamp that is needed to be put into the log.
    private void pilotLogging(int messageType, long generationTimeStamp, String data, String uuid) {
        String log;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyyMMdd");

        if(data!=null){
            //deletes some rubbish from the made messages
            data = data.replace("\\", "");
            data = data.replace(" ", "");}

        //Adds a 0  to the experiment and run number if it is below 10 like 04 and not 4.
        String experimentNumberString = (Integer.parseInt(experimentNumberText)
                < 10 ? "0" : "") + Integer.parseInt(experimentNumberText);
        String runNumberString = (Integer.parseInt(runNumberText)
                < 10 ? "0" : "") + Integer.parseInt(runNumberText);

        switch(messageType) {
            // Checks what message needs to be logged and makes a logging entry accordingly. Also
            // makes a new log file if not existing yet and adds it to the vector pointing to all
            // log files for uploading to firebase.
            case LOGGING_TAXI_SENT:
                Log.d(TAG, "pilotLogging: TAXI_SENT");
                log = ",1," + userName + "," + "SENT,CELLULAR,AutoPilot.SmartphoneTaxiRequest,"
                        + uuid + "," + userName + "," + data ;

                String fileNameTaxiSent = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_6.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameTaxiSent);
                    addHeaderToLogFile(fileNameTaxiSent);}
                if(!fileNameVector.contains(fileNameTaxiSent) && fileNameVector !=null){
                    fileNameVector.add(fileNameTaxiSent);
                    addHeaderToLogFile(fileNameTaxiSent);}


                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_6.csv",log);
                break;

            case LOGGING_TAXI_RECEIVED:
                Log.d(TAG, "pilotLogging: TAXI_RECEIVED");
                log = ",1," + userName + "," + "RECEIVED,CELLULAR,AutoPilot.SmartphoneTaxiRequest,"
                        + uuid + "," + userName + "," + data ;

                String fileNameTaxiReceived = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_7.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameTaxiReceived);
                    addHeaderToLogFile(fileNameTaxiReceived);}
                if(!fileNameVector.contains(fileNameTaxiReceived) && fileNameVector !=null){
                    fileNameVector.add(fileNameTaxiReceived);
                    addHeaderToLogFile(fileNameTaxiReceived);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_7.csv",log);

                break;

            case LOGGING_NOTNEEDED:
                break;

            case LOGGING_GPS:
                Log.d(TAG, "pilotLogging: GPS");
                log = ",1," + userName + "," + "SENT,CELLULAR,AutoPilot.SmartphoneGPS,"
                        + uuid + "," + userName + "," + data ;

                String fileNameGPS = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_1.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameGPS);
                    addHeaderToLogFile(fileNameGPS);}
                if(!fileNameVector.contains(fileNameGPS) && fileNameVector !=null){
                    fileNameVector.add(fileNameGPS);
                    addHeaderToLogFile(fileNameGPS);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_1.csv",log);

                break;

            case LOGGING_STATUS:
                Log.d(TAG, "pilotLogging: Status");
                log = ",2," + userName + "," + "SENT,CELLULAR,AutoPilot.SmartphoneUserActivity,"
                        + uuid + "," + userName + ", " + data;

                String fileNameStatus = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_2.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameStatus);
                    addHeaderToLogFile(fileNameStatus);}
                if(!fileNameVector.contains(fileNameStatus) && fileNameVector !=null){
                    fileNameVector.add(fileNameStatus);
                    addHeaderToLogFile(fileNameStatus);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_2.csv",log);
                break;

            case LOGGING_VEHICLE:
                Log.d(TAG, "pilotLogging: Vehicle");
                log = ",3," + userName + "," + "RECEIVED,CELLULAR,AutoPilot.PriusStatus," + uuid + ",112233," + data;

                String fileNameCar = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+"_3.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameCar);
                    addHeaderToLogFile(fileNameCar);}
                if(!fileNameVector.contains(fileNameCar) && fileNameVector !=null){
                    fileNameVector.add(fileNameCar);
                    addHeaderToLogFile(fileNameCar);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        +userName + "_3.csv",log);
                break;

            case LOGGING_GPS_POSEST:
                Log.d(TAG, "pilotLogging: GPS_POSEST");
                log = ",11," + userName + "," + "SENT,CELLULAR,AutoPilot.SmartphoneGPS,"
                        + uuid + "," + userName + "," + data ;

                String fileNameGPS_PosEST = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_11.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameGPS_PosEST);
                    addHeaderToLogFile(fileNameGPS_PosEST);}
                if(!fileNameVector.contains(fileNameGPS_PosEST) && fileNameVector !=null){
                    fileNameVector.add(fileNameGPS_PosEST);
                    addHeaderToLogFile(fileNameGPS_PosEST);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_" + userName
                        +"_11.csv",log);
                break;

            case LOGGING_VEHICLE_POSEST:
                Log.d(TAG, "pilotLogging: Vehicle_Posest");
                log = ",33," + userName + "," + "RECEIVED,CELLULAR,AutoPilot.PriusStatus," + uuid + ",112233," + data;

                String fileNameCarPosest = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+"_33.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameCarPosest);
                    addHeaderToLogFile(fileNameCarPosest);}
                if(!fileNameVector.contains(fileNameCarPosest) && fileNameVector !=null){
                    fileNameVector.add(fileNameCarPosest);
                    addHeaderToLogFile(fileNameCarPosest);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        +userName + "_33.csv",log);
                break;

            case LOGGING_HUAWEI_RECEIVED:
                Log.d(TAG, "pilotLogging: HuaweiReceived");

                log = ",4," + userName + "," + "RECEIVED,CELLULAR,AutoPilot." +
                        "HuaweiGeofencingRectangle," + " ,3172," + data;

                String fileNameHR = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName +"_4.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameHR);
                    addHeaderToLogFile(fileNameHR);}
                if(!fileNameVector.contains(fileNameHR) && fileNameVector !=null){
                    fileNameVector.add(fileNameHR);
                    addHeaderToLogFile(fileNameHR);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName + "_4.csv",log);
                break;

            case LOGGING_HUAWEI_SENT:
                Log.d(TAG, "pilotLogging: HuaweiSent");
                log = ",5," + userName + "," + "SENT,CELLULAR,AutoPilot.HuaweiGeofencingGPS," +
                        uuid + ','
                        + userName + "," + data;

                String fileNameHS = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+"_5.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameHS);
                    addHeaderToLogFile(fileNameHS);}
                if(!fileNameVector.contains(fileNameHS) && fileNameVector !=null){
                    fileNameVector.add(fileNameHS);
                    addHeaderToLogFile(fileNameHS);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+ "_5.csv",log);
                break;
            case LOGGING_HUAWEI_SENT_POSEST:
                Log.d(TAG, "pilotLogging: HuaweiSent_Posest");
                log = ",55," + userName + "," + "SENT,CELLULAR,AutoPilot.HuaweiGeofencingGPS," +
                        uuid + ','
                        + userName + "," + data;

                String fileNameHS_PosEst = "Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+"_55.csv";

                if(fileNameVector==null){fileNameVector.add(fileNameHS_PosEst);
                    addHeaderToLogFile(fileNameHS_PosEst);}
                if(!fileNameVector.contains(fileNameHS_PosEst) && fileNameVector !=null){
                    fileNameVector.add(fileNameHS_PosEst);
                    addHeaderToLogFile(fileNameHS_PosEst);}

                writeToLogFile("Reb_" + mdformat.format(calendar.getTime()) + "_Exp"
                        + experimentNumberString + "_Run" + runNumberString + "_"
                        + userName+ "_55.csv",log);
                break;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
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

    // Makes a header file at first for the logfile if the file did not exist yet
    private void addHeaderToLogFile(String Filename) {
        String entryFile = "log_timestamp,log_applicationid,log_stationid,log_action,log_communicationprofile,log_messagetype,log_messageuuid,stationId,data" + "\n";
        try {
            FileOutputStream out = openFileOutput(Filename,Context.MODE_APPEND);
            out.write((String.valueOf(entryFile).getBytes()));
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

    // Uploads the log files storedto firebase by using a vector as dataformat (an dynamically
    // expandable array), currently called whenever the app is paused or destroyed.
    private void uploadLogFilesFirebase() {
        if(fileNameVector!=null) {
            Log.d(TAG, "uploadLogFilesFirebase: ");
            fileNameVector.elements();
            FirebaseStorage storage = FirebaseStorage.getInstance();

            for (Enumeration<String> e = fileNameVector.elements(); e.hasMoreElements(); ) {
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
                });
            }
        }
    }


}
