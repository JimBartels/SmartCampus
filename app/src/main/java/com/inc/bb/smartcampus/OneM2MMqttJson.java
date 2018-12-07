package com.inc.bb.smartcampus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by s163310 on 4/11/2018.
 */

public class OneM2MMqttJson {

    private final static int CREATE_OP = 1;
    private final static int GET_OP = 2;
    private final static int UPDATE_OP = 3;
    private final static int DELETE_OP = 4;

    private String oneM2MAeRi;
    private String oneM2MAeRn;
    private String oneM2MAePass;
    private String userId;
    JSONObject payload = new JSONObject();
    JSONObject m2mrequester = new JSONObject();
    JSONObject contentinstancecontent = new JSONObject();
    JSONObject m2mcntrequester = new JSONObject();

    OneM2MMqttJson(String AeRI, String AeKey, String AeRN, String UserId){
        this.oneM2MAeRi = AeRI;
        this.oneM2MAePass = AeKey;
        this.oneM2MAeRn = AeRN;
        this.userId = UserId;
    }

    public JSONObject RetrieveAe() throws JSONException {
        JSONObject payload = new JSONObject();
        JSONObject m2mrequester = new JSONObject();
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        payload.put("rqi",userId);
        String topic = "/server/server/" + oneM2MAeRn;
        payload.put("to",topic);

        payload.put("op", GET_OP);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }// Creates a retrieve Ae JSON request for OneM2M

    public JSONObject RetrieveContainer(String RnContainer) throws JSONException {
        JSONObject payload = new JSONObject();
        JSONObject m2mrequester = new JSONObject();
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        payload.put("rqi",userId);
        String topic = "/server/server/" + oneM2MAeRn + "/" + RnContainer;
        payload.put("to",topic);

        payload.put("op", GET_OP);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    } // Creates a retrieve container JSON request for OneM2M

    public JSONObject RetrieveContentInstance(String RnContainer, String RnContentInstance) throws JSONException { //
        JSONObject payload = new JSONObject();
        JSONObject m2mrequester = new JSONObject();
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        payload.put("rqi",userId);
        String topic = "/server/server/" + oneM2MAeRn + "/" + RnContainer + "/" + RnContentInstance;
        payload.put("to",topic);

        payload.put("op", GET_OP);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    } // Creates a retrieve Ae JSON request for OneM2M

    public void changeAE(String newAeRi, String newPass, String newAeRn){
        this.oneM2MAePass = newPass;
        this.oneM2MAeRi = newAeRi;
        this.oneM2MAeRn = newAeRn;
    } // Changes the AE of the class and appropriate password in the JSON

    public JSONObject CreateContainer(String RnContainer) throws JSONException{
        JSONObject payload = new JSONObject();
        JSONObject m2mrequester = new JSONObject();
        JSONObject containercontent = new JSONObject();
        JSONObject m2mcntrequester = new JSONObject();
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        String topic = "/server/server/" + oneM2MAeRn + "/Users";
        payload.put("to",topic);

        payload.put("rqi",userId);
        payload.put("op", CREATE_OP);
        payload.put("ty", 3);

        containercontent.put("rn", RnContainer);
        m2mcntrequester.put("m2m:cnt", containercontent);
        payload.put("pc", m2mcntrequester);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }
    public JSONObject CreateUserContainer(String RnContainer) throws JSONException{
        JSONObject payload = new JSONObject();
        JSONObject m2mrequester = new JSONObject();
        JSONObject containercontent = new JSONObject();
        JSONObject m2mcntrequester = new JSONObject();
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        String topic = "/server/server/" + oneM2MAeRn + "/Users/" + userId;
        payload.put("to",topic);

        payload.put("rqi",userId);
        payload.put("op", CREATE_OP);
        payload.put("ty", 3);

        containercontent.put("rn", RnContainer);
        m2mcntrequester.put("m2m:cnt", containercontent);
        payload.put("pc", m2mcntrequester);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }// Creates a container create JSON request for OneM2M


    public JSONObject CreateContentInstanceStatus(String RnContentInstance,String type, int confidence) throws JSONException{
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        String topic = "/server/server/" + oneM2MAeRn + "/Users/" + userId + "/Status";
        payload.put("to",topic);

        payload.put("rqi",userId);
        payload.put("op", CREATE_OP);
        payload.put("ty", 4);

        contentinstancecontent.put("rn", RnContentInstance);
        contentinstancecontent.put("con", 0);
        m2mcntrequester.put("m2m:cin", contentinstancecontent.toString());
        payload.put("pc", m2mcntrequester);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }
    public JSONObject CreateContentInstanceGps(String RnContentInstance, Double lat, Double lng, Float Accuracy) throws JSONException{
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        String topic = "/server/server/" + oneM2MAeRn + "/Users/" + userId + "/gps";
        payload.put("to",topic);

        payload.put("rqi",userId);
        payload.put("op", CREATE_OP);
        payload.put("ty", 4);

        contentinstancecontent.put("rn", RnContentInstance);
        contentinstancecontent.put("con", "lat: " + lat + "," + " long: " + lng  + "," + " accuracy: " + Accuracy);
        m2mcntrequester.put("m2m:cin", contentinstancecontent);
        payload.put("pc", m2mcntrequester);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }
    public JSONObject CreateTaxiSubContainer() throws JSONException{
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        String topic = "/server/server/" + oneM2MAeRn + "/Users/" + userId + "/CallTaxi";
        payload.put("to",topic);
        payload.put("rqi",userId);
        payload.put("op", CREATE_OP);
        payload.put("ty", 23);

        contentinstancecontent.put("rn", "CallTaxi_sub");
        contentinstancecontent.put("nu", "Csmartcampus");
        contentinstancecontent.put("nct", "2");
        m2mcntrequester.put("m2m:sub", contentinstancecontent);
        payload.put("pc", m2mcntrequester);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }


    public JSONObject CreateContentInstanceCallTaxi(Double lat, Double lng, long timeStamp, String UserID, boolean requesting, String uuid) throws JSONException{
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        String topic = "/server/server/" + oneM2MAeRn + "/Users/" + userId + "/CallTaxi";
        payload.put("to",topic);
        payload.put("rqi",userId);
        payload.put("op", CREATE_OP);
        payload.put("ty", 4);

        JSONObject contentinstancecontentdata = new JSONObject();
        contentinstancecontentdata.put("longitude", lng);
        contentinstancecontentdata.put("latitude", lat);
        contentinstancecontentdata.put("requestTime", timeStamp);
        contentinstancecontentdata.put("UUID", uuid);
        contentinstancecontentdata.put("type", requesting ? "requesting" : "not requesting");
        contentinstancecontentdata.put("id",userId);
        contentinstancecontent.put("con",contentinstancecontentdata.toString());
        contentinstancecontent.remove("rn");
        m2mcntrequester.put("m2m:cin", contentinstancecontent);
        payload.put("pc", m2mcntrequester);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }

    public JSONObject UpdateContentInstanceCallTaxi(Double lat, Double lng, long timeStamp, String UserID, boolean requesting, String uuid) throws JSONException{
        payload.put("fr",oneM2MAeRi);
        payload.put("key",oneM2MAePass);

        String topic = "/server/server/" + oneM2MAeRn + "/Users/" + userId + "/CallTaxi/taxi";
        payload.put("to",topic);
        payload.put("rqi",userId);
        payload.put("op", UPDATE_OP);
        payload.put("ty", 4);

        JSONObject contentinstancecontentdata = new JSONObject();
        contentinstancecontentdata.put("longitude", lng);
        contentinstancecontentdata.put("latitude", lat);
        contentinstancecontentdata.put("requestTime", timeStamp);
        contentinstancecontentdata.put("UUID", uuid);
        contentinstancecontentdata.put("type", requesting ? "requesting" : "not requesting");
        contentinstancecontentdata.put("id", userId);
        contentinstancecontent.put("con",contentinstancecontentdata.toString());
        contentinstancecontent.remove("rn");
        m2mcntrequester.put("m2m:cin", contentinstancecontent);
        payload.put("pc", m2mcntrequester);
        m2mrequester.put("m2m:rqp",payload);
        return m2mrequester;
    }



    //Creates a content instance create JSON request for OneM2M
    //TODO update functions



}
